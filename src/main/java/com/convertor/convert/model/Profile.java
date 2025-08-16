package com.convertor.convert.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Table(name = "profile")
public class Profile {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "profile_template_id")
    private String profileTemplateId; // source template id used to create this profile (snapshot origin)

    @Column(name = "profile_template_version")
    private Integer profileTemplateVersion;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private List<Field> fields;
}
