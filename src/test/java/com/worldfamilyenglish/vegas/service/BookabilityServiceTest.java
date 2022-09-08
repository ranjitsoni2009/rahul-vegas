package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.ContentPublishingPlan;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookabilityServiceTest {

    @Test
    public void GIVEN_seats_with_many_content_levels_AND_a_given_cap_level_EXPECT_only_seats_that_match_the_cap_level_are_returned() {

        ContentService mockContentService = mock(ContentService.class);
        LessonService mockLessonService = mock(LessonService.class);
        PeriodService mockPeriodService = mock(PeriodService.class);

        List<ContentPublishingPlan> testPublishingPlans = List.of(createTestPublishingPlans(CapLevel.BLUE, "2022-01-01T00:00Z", "2022-12-30T00:00Z"),
                createTestPublishingPlans(CapLevel.GREEN, "2022-01-01T00:00Z", "2022-12-30T00:00Z"),
                createTestPublishingPlans(CapLevel.ORANGE, "2022-01-01T00:00Z", "2022-12-30T00:00Z"));

        when(mockContentService.getAllPublishingPlans(notNull(), notNull())).thenReturn(testPublishingPlans);


        BookabilityService serviceUnderTest = new BookabilityService(mockContentService, mockLessonService, mockPeriodService);

        serviceUnderTest.findBookability(CapLevel.BLUE, OffsetDateTime.parse("2022-04-01T00:00Z"), OffsetDateTime.parse("2022-04-30T00:00Z"));

        ArgumentCaptor<ContentInfo> planCaptor = ArgumentCaptor.forClass(ContentInfo.class);

        verify(mockLessonService).findSeats(planCaptor.capture(), eq(OffsetDateTime.parse("2022-04-01T00:00Z")), eq(OffsetDateTime.parse("2022-04-30T00:00Z")));


        ContentInfo capturedContentInfo = planCaptor.getValue();
        assertThat(capturedContentInfo.getLevel()).isEqualTo(CapLevel.BLUE);
        assertThat(capturedContentInfo).isEqualTo(testPublishingPlans.get(0).getContent());

    }

    private ContentPublishingPlan createTestPublishingPlans(final CapLevel capLevel, final String from, final String to) {
        ContentInfo contentInfo = ContentInfo.builder().level(capLevel).name("Some content " + capLevel.toString()).build();
        return ContentPublishingPlan.builder().content(contentInfo).from(OffsetDateTime.parse(from)).to(OffsetDateTime.parse(to)).build();
    }
}