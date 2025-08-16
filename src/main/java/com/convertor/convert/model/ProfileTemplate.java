package com.convertor.convert.model;



import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_template")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileTemplate {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "template_version", nullable = false)
    private Integer templateVersion = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Root field template id(s) for this profile template
    @ElementCollection
    @CollectionTable(name = "profile_template_root_fields", joinColumns = @JoinColumn(name = "profile_template_id"))
    @Column(name = "field_template_id")
    private java.util.Set<String> rootFieldTemplateIds;
}
