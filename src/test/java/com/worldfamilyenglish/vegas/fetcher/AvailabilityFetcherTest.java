package com.worldfamilyenglish.vegas.fetcher;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.service.AvailabilityService;
import com.worldfamilyenglish.vegas.types.QueryAvailabilityFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityFetcherTest {

    private AvailabilityService _mockAvailabilityService;
    private AvailabilityFetcher _testFetcher;

    @BeforeEach
    void setUp() {
        _mockAvailabilityService = mock(AvailabilityService.class);
        _testFetcher = new AvailabilityFetcher(_mockAvailabilityService);

    }

    @Test
    public void GIVEN_service_returns_empty_list_EXPECT_fetcher_also_returns_empty_list() {

        when(_mockAvailabilityService.getRange(notNull(), notNull())).thenReturn(List.of());

        List<Availability> availability = _testFetcher.availability(null);

        assertThat(availability).isNotNull();
        assertThat(availability).isEmpty();
    }

    @Test
    public void GIVEN_filter_is_null_EXPECT_default_date_ranges_are_passed_to_service() {
        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        _testFetcher.availability(null);

        verify(_mockAvailabilityService).getRange(fromCaptor.capture(), toCaptor.capture());

        OffsetDateTime expectedFromDate = OffsetDateTime.now().minusDays(7);
        OffsetDateTime expectedToDate = OffsetDateTime.now().plusDays(14);


        assertThat(fromCaptor.getValue()).isEqualToIgnoringMinutes(expectedFromDate);
        assertThat(toCaptor.getValue()).isEqualToIgnoringMinutes(expectedToDate);
    }

    @Test
    public void GIVEN_filter_is_provided_EXPECT_service_to_be_provided_filter_values() {
        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);


        when(_mockAvailabilityService.getRange(notNull(), notNull())).thenReturn(List.of());

        String fromDate = "2021-01-01T00:00:00Z";
        String toDate = "2024-12-31T23:59:59Z";

        QueryAvailabilityFilter filter = QueryAvailabilityFilter
                .newBuilder()
                .from(OffsetDateTime.parse(fromDate))
                .to(OffsetDateTime.parse(toDate))
                .build();

        _testFetcher.availability(filter);

        verify(_mockAvailabilityService).getRange(fromCaptor.capture(), toCaptor.capture());

        assertThat(fromCaptor.getValue()).isEqualToIgnoringMinutes(OffsetDateTime.parse(fromDate));
        assertThat(toCaptor.getValue()).isEqualToIgnoringMinutes(OffsetDateTime.parse(toDate));
    }
}