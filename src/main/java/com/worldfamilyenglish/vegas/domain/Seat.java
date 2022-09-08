package com.worldfamilyenglish.vegas.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Accessors(prefix = "")
@Entity
@Getter
@Setter
@ToString
@Builder
@Table(name = "seat")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seat {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "content_id")
    @EqualsAndHashCode.Include
    private ContentInfo content;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "booking_id")
    @Setter(AccessLevel.NONE)
    private Booking booking;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Version
    private Long version;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Setter(AccessLevel.PACKAGE)
    @EqualsAndHashCode.Include
    private Lesson lesson;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;

    public boolean hasBooking() {
        return booking != null;
    }

    public void setBooking(final Booking booking) {
        this.booking = booking;
        booking.setSeat(this);
    }

    public void clearBooking() {
        if (booking == null) {
            return;
        }

        booking.clearSeat();
        booking = null;
    }
}

