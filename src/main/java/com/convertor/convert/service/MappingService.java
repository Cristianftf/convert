package com.convertor.convert.service;

import com.convertor.convert.model.Field;
import com.convertor.convert.model.FieldTemplate;
import com.convertor.convert.model.Profile;
import com.convertor.convert.model.ProfileTemplate;
import com.convertor.convert.repository.FieldRepository;
import com.convertor.convert.repository.FieldTemplateRepository;
import com.convertor.convert.repository.ProfileTemplateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MappingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProfileTemplateRepository profileTemplateRepository;
    private final FieldTemplateRepository fieldTemplateRepository;
    private final FieldRepository fieldRepository;
    // NOTE: no ProfileRepository here; Writer persistirá el Profile

    @Transactional
    public Profile transformRawJsonToProfile(String rawJson, String profileTemplateId) throws Exception {
        JsonNode rootNode = objectMapper.readTree(rawJson);

        // Resolver template id (usar variable temporal)
        String tmp = profileTemplateId;
        if (tmp == null || tmp.isBlank()) {
            JsonNode tid = rootNode.get("targetProfileTemplateId");
            if (tid != null && !tid.isNull()) {
                tmp = tid.asText();
            }
        }
        if (tmp == null || tmp.isBlank()) {
            throw new IllegalArgumentException("No se especificó profileTemplateId (ni en jobParameters ni en payload).");
        }
        final String resolvedTemplateIdFinal = tmp; // final para usar en lambdas

        ProfileTemplate profileTemplate = profileTemplateRepository.findById(resolvedTemplateIdFinal)
                .orElseThrow(() -> new IllegalArgumentException("ProfileTemplate no encontrado: " + resolvedTemplateIdFinal));

        Profile profile = Profile.builder()
                .id(UUID.randomUUID().toString())
                .profileTemplateId(profileTemplate.getId())
                .profileTemplateVersion(profileTemplate.getTemplateVersion())
                .name(rootNode.path("name").asText(null))
                .createdAt(Instant.now())
                .build();

        // Lista para recolectar todos los Fields y guardarlos en lote al final
        List<Field> allFieldsToSave = new ArrayList<>();
        List<Field> topFields = new ArrayList<>();

        Set<String> rootIds = profileTemplate.getRootFieldTemplateIds();
        if (rootIds != null) {
            for (String rootFieldTemplateId : rootIds) {
                FieldTemplate ft = fieldTemplateRepository.findById(rootFieldTemplateId)
                        .orElseThrow(() -> new IllegalStateException("FieldTemplate raíz no existe: " + rootFieldTemplateId));
                JsonNode valueNode = rootNode.get(ft.getName());
                // craftFieldFromTemplate ahora devuelve el Field, no lo guarda
                Field crafted = craftFieldFromTemplate(ft, valueNode, profile.getId(), allFieldsToSave);
                topFields.add(crafted);
            }
        }

        profile.setFields(topFields);
        
        // Guardar todos los Fields en lote para mejorar el rendimiento
        fieldRepository.saveAll(allFieldsToSave);

        return profile;
    }

    private Field craftFieldFromTemplate(FieldTemplate template, JsonNode valueNode, String profileId, List<Field> allFieldsToSave) throws Exception {
        Field field = Field.builder()
                .id(UUID.randomUUID().toString())
                .profileId(profileId)
                .sourceTemplateId(template.getId())
                .sourceTemplateVersion(template.getTemplateVersion())
                .typeField(template.getTypeField())
                .createdAt(Instant.now())
                .build();

        JsonNode ruleNode = null;
        if (template.getConstructionRuleJson() != null && !template.getConstructionRuleJson().isBlank()) {
            ruleNode = objectMapper.readTree(template.getConstructionRuleJson());
        }

        String type = (template.getTypeField() == null) ? "TEXT" : template.getTypeField().toUpperCase(Locale.ROOT);

        boolean nullable = true;
        if (ruleNode != null && ruleNode.has("type") && ruleNode.get("type").has("nullable")) {
            nullable = ruleNode.get("type").get("nullable").asBoolean(true);
        }
        if (!nullable && (valueNode == null || valueNode.isNull())) {
            throw new IllegalArgumentException("Campo obligatorio ausente para template: " + template.getName());
        }

        switch (type) {
            case "TEXT":
            case "NUMBER":
            case "BOOLEAN":
            case "DATE":
            case "DATETIME": {
                validatePrimitive(ruleNode, template, valueNode);

                if (valueNode != null && !valueNode.isNull()) {
                    String val = valueNode.asText(null);
                    if (val != null) val = normalizeValueText(val);
                    field.setValue(val);
                } else {
                    field.setValue(null);
                }
                break;
            }

            case "TAXONOMY": {
                if (valueNode != null && !valueNode.isNull()) {
                    String val = valueNode.asText(null);
                    val = (val == null) ? null : normalizeValueText(val);
                    field.setValue(val);
                }
                break;
            }

            case "CUSTOM_OBJECT": {
                if (ruleNode != null && ruleNode.has("structure")) {
                    JsonNode structure = ruleNode.get("structure");
                    Iterator<String> attrs = structure.fieldNames();
                    Map<String, String> objectValuesMap = new LinkedHashMap<>();
                    while (attrs.hasNext()) {
                        String attr = attrs.next();
                        String childTemplateId = structure.get(attr).asText();
                        FieldTemplate childTemplate = fieldTemplateRepository.findById(childTemplateId)
                                .orElseThrow(() -> new IllegalStateException("Child template no encontrado: " + childTemplateId));

                        if (childTemplate.getAncestorsSet() != null && childTemplate.getAncestorsSet().contains(template.getId())) {
                            throw new IllegalStateException("Posible ciclo detectado entre templates: " + template.getId() + " <-> " + childTemplateId);
                        }

                        JsonNode childValueNode = (valueNode != null) ? valueNode.get(attr) : null;
                        // Pasar allFieldsToSave a la llamada recursiva
                        Field childField = craftFieldFromTemplate(childTemplate, childValueNode, profileId, allFieldsToSave);
                        objectValuesMap.put(attr, childField.getId());
                    }
                    field.setObjectValuesJson(objectMapper.writeValueAsString(objectValuesMap));
                }
                break;
            }

            case "LIST_FIXED": {
                if (ruleNode != null && ruleNode.has("listStructure")) {
                    JsonNode listStructure = ruleNode.get("listStructure");
                    Map<Integer, String> fixedMap = new LinkedHashMap<>();
                    int idx = 0;
                    for (JsonNode childIdNode : listStructure) {
                        String childTemplateId = childIdNode.asText();
                        FieldTemplate childTemplate = fieldTemplateRepository.findById(childTemplateId)
                                .orElseThrow(() -> new IllegalStateException("Child template no encontrado: " + childTemplateId));
                        JsonNode childValueNode = (valueNode != null && valueNode.isArray() && valueNode.size() > idx) ? valueNode.get(idx) : null;
                        // Pasar allFieldsToSave a la llamada recursiva
                        Field childField = craftFieldFromTemplate(childTemplate, childValueNode, profileId, allFieldsToSave);
                        fixedMap.put(idx, childField.getId());
                        idx++;
                    }
                    field.setObjectValuesJson(objectMapper.writeValueAsString(fixedMap));
                }
                break;
            }

            case "LIST_OF": {
                if (ruleNode != null && ruleNode.has("itemTemplateId")) {
                    String itemTemplateId = ruleNode.get("itemTemplateId").asText();
                    FieldTemplate itemTemplate = fieldTemplateRepository.findById(itemTemplateId)
                            .orElseThrow(() -> new IllegalStateException("Item template no encontrado: " + itemTemplateId));
                    List<String> itemIds = new ArrayList<>();
                    if (valueNode != null && valueNode.isArray()) {
                        int counter = 0;
                        for (JsonNode itemValNode : valueNode) {
                            JsonNode itemNodeForProcessing = itemValNode; // Usar el nodo original por defecto
                            
                            // Evitar la modificación del ObjectNode original:
                            // Si itemValNode es un objeto y tiene un campo "value" que necesita normalización,
                            // creamos una copia para modificarla, dejando el original intacto.
                            if (itemValNode instanceof ObjectNode && itemValNode.has("value")) {
                                JsonNode rawVal = itemValNode.get("value");
                                if (rawVal != null && !rawVal.isNull()) {
                                    String normalized = normalizeValueText(rawVal.asText());
                                    ObjectNode copiedNode = objectMapper.createObjectNode();
                                    copiedNode.setAll((ObjectNode) itemValNode); // Copiar todos los campos del original
                                    copiedNode.put("value", normalized); // Modificar solo el campo 'value' en la copia
                                    itemNodeForProcessing = copiedNode;
                                }
                            }
                            // Pasar allFieldsToSave a la llamada recursiva
                            Field itemField = craftFieldFromTemplate(itemTemplate, itemNodeForProcessing, profileId, allFieldsToSave);
                            itemIds.add(itemField.getId());

                            if (++counter % 1000 == 0) {
                                System.out.println("Processed " + counter + " items for list field template " + template.getId());
                            }
                        }
                    }
                    field.setListItemIds(itemIds);
                }
                break;
            }

            default: {
                if (valueNode != null && !valueNode.isNull()) {
                    field.setValue(valueNode.toString());
                }
            }
        }
        
        // Añadir el Field actual a la lista para guardado en lote
        allFieldsToSave.add(field);
        return field; // Retornar el Field para que se añada a la lista de topFields o a las relaciones
    }

    private void validatePrimitive(JsonNode ruleNode, FieldTemplate template, JsonNode valueNode) {
        if (ruleNode == null) return;
        JsonNode typeNode = ruleNode.get("type");
        if (typeNode == null) typeNode = ruleNode;

        if (valueNode == null || valueNode.isNull()) return;

        String text = valueNode.asText("");

        if (typeNode != null && typeNode.has("limits")) {
            JsonNode limits = typeNode.get("limits");
            if (limits.has("max")) {
                int max = limits.get("max").asInt(Integer.MAX_VALUE);
                if (text.length() > max) {
                    throw new IllegalArgumentException(String.format("Campo '%s' excede longitud máxima %d", template.getName(), max));
                }
            }
            if (limits.has("min")) {
                int min = limits.get("min").asInt(0);
                if (text.length() < min) {
                    throw new IllegalArgumentException(String.format("Campo '%s' tiene longitud menor que %d", template.getName(), min));
                }
            }
        }

        if (typeNode != null && typeNode.has("pattern")) {
            String regex = typeNode.get("pattern").asText();
            if (regex != null && !regex.isBlank()) {
                Pattern p = Pattern.compile(regex);
                if (!p.matcher(text).matches()) {
                    throw new IllegalArgumentException(String.format("Campo '%s' no cumple patrón %s", template.getName(), regex));
                }
            }
        }

        if ("TAXONOMY".equalsIgnoreCase(template.getTypeField()) && typeNode != null && typeNode.has("values")) {
            JsonNode values = typeNode.get("values");
            boolean found = false;
            Iterator<String> keys = values.fieldNames();
            while (keys.hasNext()) {
                String k = keys.next();
                JsonNode v = values.get(k);
                if (v != null && v.asText().equalsIgnoreCase(text)) {
                    found = true;
                    break;
                }
            }
            if (!found && values.size() > 0) {
                throw new IllegalArgumentException(String.format("Campo '%s' con valor '%s' no existe en taxonomía definida", template.getName(), text));
            }
        }
    }

    private String normalizeValueText(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    @SuppressWarnings("unused")
    private Instant parseDateNodeToInstant(JsonNode dateNode) {
        if (dateNode == null || dateNode.isNull()) return null;
        try {
            int year = dateNode.path("year").asInt();
            int month = dateNode.path("monthValue").asInt();
            int day = dateNode.path("dayOfMonth").asInt();
            int hour = dateNode.path("hour").asInt();
            int minute = dateNode.path("minute").asInt();
            int second = dateNode.path("second").asInt();
            int nano = dateNode.path("nano").asInt(0);
            LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second, nano);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (Exception ex) {
            return null;
        }
    }
}