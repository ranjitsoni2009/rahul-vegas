package com.worldfamilyenglish.vegas.domain;


import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Accessors(prefix = "")
@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@Table(name = "availability")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_time")
    @EqualsAndHashCode.Include
    @ToString.Include
    private OffsetDateTime from;

    @Column(name = "to_time")
    @EqualsAndHashCode.Include
    @ToString.Include
    private OffsetDateTime to;

    @Column(name = "is_bookable")
    private boolean isBookable;

    @ManyToOne
    @JoinColumn(name="owner", nullable=false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private DomainUser owner;

    @OneToMany(mappedBy = "owningAvailability")
    private List<Period> periods;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;

}
