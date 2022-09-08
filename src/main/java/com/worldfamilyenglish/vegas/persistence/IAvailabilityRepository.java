package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface IAvailabilityRepository extends JpaRepository<Availability, Long> {

    /**
     * Find any availability where the start of the block is between the given start and end range dates.
     *
     * If the query dates full within an availability block,
     *
     * @param queryStart the start date range to scan for availability
     * @param queryEnd
     * @return any availability that overlaps with the query.
     */
    List<Availability> findByToGreaterThanEqualAndFromLessThanEqual(final OffsetDateTime queryStart, final OffsetDateTime queryEnd);

    /**
     * Find any availability for the given range, for the given owner.
     * @param startTime the start time to look for.
     * @param endTime the end time to look for.
     * @param owner the owner of the availability
     * @return a list of found availability
     */
    List<Availability> findByToGreaterThanEqualAndFromLessThanEqualAndOwner(final OffsetDateTime startTime, final OffsetDateTime endTime, final DomainUser owner);
}
