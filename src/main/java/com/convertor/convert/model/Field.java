package com.convertor.convert.model;



import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "profile_id", nullable = false)
    private String profileId;

    @Column(name = "source_template_id")
    private String sourceTemplateId;

    @Column(name = "source_template_version")
    private Integer sourceTemplateVersion;

    @Column(name = "type_field", nullable = false)
    private String typeField;

    // For primitive types (TEXT, NUMBER, BOOLEAN, DATE...) store value as string (parse según typeField).
    @Column(name = "value", length = 2000)
    private String value;

    // For richer structures, serialize nested structure or child references in JSON:
    @Lob
    @Column(name = "object_values", columnDefinition = "text")
    private String objectValuesJson; // map attribute -> childFieldId (or directly embedded values)

    @ElementCollection
    @CollectionTable(name = "field_list_items", joinColumns = @JoinColumn(name = "field_id"))
    @Column(name = "item_field_id")
    private List<String> listItemIds; // if LIST_OF, store created child field ids

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
