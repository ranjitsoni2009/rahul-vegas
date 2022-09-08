package com.worldfamilyenglish.vegas.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.*;
import lombok.experimental.Accessors;

@Accessors(prefix = "")
@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@Builder
@Table(name = "lesson")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long id;

    @Version
    private Long version;

    @OneToOne
    @JoinColumn(name = "period_id")
    @ToString.Include
    @EqualsAndHashCode.Include
    private Period period;

    @ManyToOne
    @JoinColumn(name = "content_id")
    @EqualsAndHashCode.Include
    private ContentInfo content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lesson", orphanRemoval = true)
    @Setter(value = AccessLevel.PRIVATE)
    private List<Seat> seats;

    @Column(name = "zoom_meeting_url")
    private String zoomMeetingUrl;

    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;

    /**
     * Adds a seat to the lesson and parents the lesson on the seat
     * @param seat
     */
    public void addSeat(final Seat seat) {
        if (seats == null) {
            seats = new ArrayList<>();
        }

        seats.add(seat);
        seat.setLesson(this);
    }

    public boolean hasAnyBookings() {
        for (Seat seat : seats) {
            if (seat.hasBooking()) {
                return true;
            }
        }

        return false;
    }

    void clearPeriod() {
        period = null;
    }
}
