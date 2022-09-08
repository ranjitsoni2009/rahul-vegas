package com.worldfamilyenglish.vegas.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Accessors(prefix = "")
@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@Table(name = "booking")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    @ToString.Include
    private BookingStatus status;

    @OneToOne
    @JoinColumn(name = "seat_id")
    @Setter(AccessLevel.PACKAGE)
    private Seat seat;

    @ManyToOne
    @JoinColumn(name = "child_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Child child;

    @Column(name = "timeout")
    private OffsetDateTime timeout;

    @CreationTimestamp
    @Column(name = "created")
    @EqualsAndHashCode.Include
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;

    public void clearSeat() {
        seat = null;
    }
}
