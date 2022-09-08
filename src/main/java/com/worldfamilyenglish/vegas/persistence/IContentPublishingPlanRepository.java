package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.ContentPublishingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface IContentPublishingPlanRepository extends JpaRepository<ContentPublishingPlan, Long> {

    List<ContentPublishingPlan> findByFromLessThanEqualAndToGreaterThanEqualAndContent_Id(final OffsetDateTime from, final OffsetDateTime to, final Long contentId);

    /**
     * Find all {@link ContentPublishingPlan} that overlap with the given search window.
     * @param from the range to check from
     * @param to the range to check to
     * @return all content publishing plans that overlap the search window.
     */
    List<ContentPublishingPlan> findByToGreaterThanEqualAndFromLessThanEqual(final OffsetDateTime from, final OffsetDateTime to);
}
