package com.worldfamilyenglish.vegas.domain;


import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

@Accessors(prefix = "")
@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@Table(name = "period")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Period {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * If the period is directly bookable, or can only be assigned a class manually.
     */
    @Column(name = "is_bookable")
    private boolean isBookable;

    @Column(name = "from_time")
    @EqualsAndHashCode.Include
    @ToString.Include
    private OffsetDateTime from;

    @Column(name = "to_time")
    @EqualsAndHashCode.Include
    @ToString.Include
    private OffsetDateTime to;

    @Version
    private Long version;

    /**
     * The lesson attached to this period. May be null if no-one has booked anything for the period.
     */
    @OneToOne(mappedBy = "period")
    @Setter(AccessLevel.NONE)
    private Lesson lesson;

    @NotNull
    @ManyToOne
    @EqualsAndHashCode.Include
    @ToString.Include
    private DomainUser owner;

    @NotNull
    @ManyToOne
    private Availability owningAvailability;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PeriodType type;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;


    public void clearLesson() {
        if (lesson == null) {
            return;
        }

        lesson.clearPeriod();
        lesson = null;
    }

    public void setLesson(final Lesson lesson) {
        this.lesson = lesson;
        lesson.setPeriod(this);
    }
}
