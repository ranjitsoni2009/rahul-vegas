package com.worldfamilyenglish.vegas.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Accessors(prefix = "")
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "content_info")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cap_level")
    @EqualsAndHashCode.Include
    private CapLevel level;

    @Column(name = "name")
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "external_url")
    private String externalUrl;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;
}
