package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;

public interface IPeriodRepository extends JpaRepository<Period, Long> {

    /**
     * Find a period at the given start time, where the period is bookable, doesn't have a lesson
     * and where period's owner has an accreditation for performing the content.
     * @param from
     * @return
     */
    List<Period> findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(final OffsetDateTime from, final ContentInfo contentInfo);

    /**
     * Find a bunch of periods within the given period, where the period is bookable, doesn't have a lesson
     * and where the period's owner has an accreditation for performing the content.
     * @param from the query window to start from
     * @param to the query window to end on
     * @param contentInfo the content the owner must be able to teach.
     * @return a list of {@Link Period}s that match the query.
     */
    List<Period> findByFromGreaterThanEqualAndToLessThanEqualAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(final OffsetDateTime from, final OffsetDateTime to, final ContentInfo contentInfo);

}
