package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.ContentPublishingPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class IContentPublishingPlanRepositoryTest {

    @Autowired
    private IContentPublishingPlanRepository _testRepository;

    @Autowired IContentInfoRepository _contentInfoRepository;

    private ContentInfo _testBlueContent;
    private ContentInfo _testYellowContent;
    private ContentPublishingPlan _yellowPlan;
    private ContentPublishingPlan _bluePlan;

    @BeforeEach
    void setUp() {
        ContentInfo some_blue_content = ContentInfo.builder().level(CapLevel.BLUE).name("Some blue content").externalUrl("http://example.com/content.pdf").build();
        ContentInfo some_yellow_content = ContentInfo.builder().level(CapLevel.YELLOW).name("My yellow content").externalUrl("http://example.com/presentation.pdf").build();
        _testBlueContent = _contentInfoRepository.save(some_blue_content);
        _testYellowContent = _contentInfoRepository.save(some_yellow_content);

        _bluePlan = ContentPublishingPlan
                .builder()
                .from(OffsetDateTime.parse("2022-04-01T00:00Z"))
                .to(OffsetDateTime.parse("2022-04-30T11:59Z"))
                .content(_testBlueContent)
                .build();

        _yellowPlan = ContentPublishingPlan
                .builder()
                .from(OffsetDateTime.parse("2022-04-15T00:00Z"))
                .to(OffsetDateTime.parse("2022-04-30T11:59Z"))
                .content(_testYellowContent)
                .build();



        _testRepository.save(_bluePlan);
        _testRepository.save(_yellowPlan);

    }

    @Test
    public void GIVEN_plans_are_available_but_query_time_is_wrong_EXPECT_no_plan_is_found() {

        OffsetDateTime queryTimeBeforePlanTime = OffsetDateTime.parse("2022-03-15T15:00Z");
        List<ContentPublishingPlan> foundPlans = _testRepository.findByFromLessThanEqualAndToGreaterThanEqualAndContent_Id(queryTimeBeforePlanTime, queryTimeBeforePlanTime, _testBlueContent.getId());

        assertThat(foundPlans).isEmpty();
    }

    @Test
    public void GIVEN_plans_are_available_and_query_time_is_within_bounds_EXPECT_plan_is_found() {
        OffsetDateTime queryTimeWithinPlanBounds = OffsetDateTime.parse("2022-04-15T15:00Z");
        List<ContentPublishingPlan> foundPlans = _testRepository.findByFromLessThanEqualAndToGreaterThanEqualAndContent_Id(queryTimeWithinPlanBounds, queryTimeWithinPlanBounds, _testBlueContent.getId());

        assertThat(foundPlans).hasSize(1);

        ContentPublishingPlan foundPlan = foundPlans.get(0);
        assertThat(foundPlan.getContent()).isEqualTo(_testBlueContent);
        assertThat(foundPlan.getFrom()).isEqualTo("2022-04-01T00:00Z");
        assertThat(foundPlan.getTo()).isEqualTo("2022-04-30T11:59Z");
    }

    @Test
    public void GIVEN_publishing_start_time_and_query_time_are_the_same_EXPECT_plan_is_still_found() {
        OffsetDateTime queryTimeWithinPlanBounds = OffsetDateTime.parse("2022-04-01T00:00Z");
        List<ContentPublishingPlan> foundPlans = _testRepository.findByFromLessThanEqualAndToGreaterThanEqualAndContent_Id(queryTimeWithinPlanBounds, queryTimeWithinPlanBounds, _testBlueContent.getId());

        assertThat(foundPlans).hasSize(1);

        ContentPublishingPlan foundPlan = foundPlans.get(0);
        assertThat(foundPlan.getContent()).isEqualTo(_testBlueContent);
        assertThat(foundPlan.getFrom()).isEqualTo("2022-04-01T00:00Z");
        assertThat(foundPlan.getTo()).isEqualTo("2022-04-30T11:59Z");
    }

    @Test
    public void GIVEN_small_query_window_EXPECT_find_overlapping_publishing_plans() {

        OffsetDateTime smallWindowFrom = OffsetDateTime.parse("2022-04-13T00:00Z");
        OffsetDateTime smallWindowTo = OffsetDateTime.parse("2022-04-16T23:59Z");

        List<ContentPublishingPlan> foundPlans = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(smallWindowFrom, smallWindowTo);

        assertThat(foundPlans).hasSize(2);
        assertThat(foundPlans).contains(_bluePlan, _yellowPlan);
    }

    @Test
    public void GIVEN_query_window_is_wider_than_plans_EXPECT_find_all_overlapping_publishing_plans() {
        OffsetDateTime wideWindowFrom = OffsetDateTime.parse("2022-04-01T00:00Z");
        OffsetDateTime wideWindowTo = OffsetDateTime.parse("2022-04-30T23:59Z");

        List<ContentPublishingPlan> foundPlans = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(wideWindowFrom, wideWindowTo);

        assertThat(foundPlans).hasSize(2);
        assertThat(foundPlans).contains(_bluePlan, _yellowPlan);
    }

}