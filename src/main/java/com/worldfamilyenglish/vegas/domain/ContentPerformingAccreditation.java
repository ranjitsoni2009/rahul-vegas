package com.worldfamilyenglish.vegas.domain;

import liquibase.pro.packaged.E;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Accessors(prefix = "")
@Builder
@Getter
@Setter
@Entity
@Table(name = "content_performing_accreditation")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentPerformingAccreditation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The user that is accredited to teach the content.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Include
    private DomainUser domainUser;

    @ManyToOne
    @JoinColumn(name = "content_id")
    @EqualsAndHashCode.Include
    private ContentInfo contentInfo;

    @Column(name = "is_accredited")
    private boolean isAccredited;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;

}
