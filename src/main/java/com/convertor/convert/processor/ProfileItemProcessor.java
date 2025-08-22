package com.convertor.convert.processor;

import com.convertor.convert.dto.ProfileTemplateDTO;
import com.convertor.convert.dto.FieldTemplateDTO;
import com.convertor.convert.model.ConstructionRule;
import com.convertor.convert.model.TypeField;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class ProfileItemProcessor implements ItemProcessor<String, ProfileTemplateDTO> {

    @Value("#{jobParameters['profileName']}")
    private String profileName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProfileTemplateDTO process(@NonNull String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode valuesNode = root.path("values");
        
        if (!valuesNode.isArray()) {
            throw new IllegalArgumentException("El JSON no contiene un array 'values'");
        }

        // Crear FieldTemplateDTO con ConstructionRule
        FieldTemplateDTO fieldTemplate = new FieldTemplateDTO();
        fieldTemplate.setName(profileName + " Field");
        
        ConstructionRule constructionRule = new ConstructionRule();
        constructionRule.setTypeField(TypeField.LIST);
        
        List<String> valueList = new ArrayList<>();
        for (JsonNode valueNode : valuesNode) {
            valueList.add(valueNode.path("value").asText());
        }
        constructionRule.setValues(valueList);
        
        fieldTemplate.setConstructionRule(constructionRule);

        // Crear ProfileTemplateDTO
        ProfileTemplateDTO profileTemplate = new ProfileTemplateDTO();
        profileTemplate.setName(profileName);
        profileTemplate.setFieldTemplate(fieldTemplate);

        return profileTemplate;
    }
}