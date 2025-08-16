package com.convertor.convert.model;



import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "field_template")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FieldTemplate {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "profile_template_id", nullable = false)
    private String profileTemplateId;

    @Column(nullable = false)
    private String name; // "edad", "nombre", etc.

    @Column(name = "type_field", nullable = false)
    private String typeField; // TEXT, NUMBER, CUSTOM_OBJECT, LIST_OF, LIST_FIXED, TAXONOMY, DATE, ...

    // JSON textual representation of constructionRule: structure, listStructure, limits, values, pattern, nullable ...
    @Lob
    @Column(name = "construction_rule", columnDefinition = "text")
    private String constructionRuleJson;

    // ascendanceLists: array of arrays serialized as JSON string
    @Lob
    @Column(name = "ascendance_lists", columnDefinition = "text")
    private String ascendanceListsJson;

    // Flattened set of ancestor IDs (facilita consulta)
    @ElementCollection
    @CollectionTable(name = "field_template_ancestors", joinColumns = @JoinColumn(name = "field_template_id"))
    @Column(name = "ancestor_id")
    private Set<String> ancestorsSet;

    @Column(name = "parent_ids")
    private String parentIds; // opcional serializado (o maneja con otra ElementCollection)

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "template_version", nullable = false)
    private Integer templateVersion = 1;

    @Column(name = "labels", length = 2000)
    private String labelsJson; // i18n labels JSON

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
