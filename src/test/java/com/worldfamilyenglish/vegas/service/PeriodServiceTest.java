package com.worldfamilyenglish.vegas.service;

import static java.time.OffsetDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.PeriodType;
import com.worldfamilyenglish.vegas.persistence.IPeriodRepository;

class PeriodServiceTest {

    private IPeriodRepository _mockPeriodRepository;
    private PeriodService _testService;

    @BeforeEach
    void setUp() {
        _mockPeriodRepository = mock(IPeriodRepository.class);
        _testService = new PeriodService(_mockPeriodRepository);
    }

    @Test
    public void GIVEN_valid_dates_with_period_is_bookable_EXPECT_periods_are_created_for_date_ranges() {
        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T16:00Z", true);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(9);

        Period checkinPeriod = periods.get(0);
        assertIsValidCheckInPeriod(checkinPeriod, "2021-05-30T11:45Z", "2021-05-30T12:00Z", true);

        Period firstPeriod = periods.get(1);
        assertThat(firstPeriod.getFrom()).isEqualTo(parse("2021-05-30T12:00Z"));
        assertThat(ChronoUnit.MINUTES.between(firstPeriod.getFrom(), firstPeriod.getTo())).isEqualTo(30);
        assertThat(firstPeriod.isBookable()).isEqualTo(true);
        assertThat(firstPeriod.getType()).isEqualTo(PeriodType.TEACHING);

        Period secondPeriod = periods.get(2);
        assertThat(secondPeriod.getFrom()).isEqualTo(parse("2021-05-30T12:30Z"));
        assertThat(ChronoUnit.MINUTES.between(secondPeriod.getFrom(), secondPeriod.getTo())).isEqualTo(30);
        assertThat(secondPeriod.isBookable()).isEqualTo(true);
        assertThat(secondPeriod.getType()).isEqualTo(PeriodType.TEACHING);


        Period penultimatePeriod = periods.get(7);
        assertThat(penultimatePeriod.getFrom()).isEqualTo(parse("2021-05-30T15:00Z"));
        assertThat(penultimatePeriod.getTo()).isEqualTo(parse("2021-05-30T15:30Z"));
        assertThat(penultimatePeriod.isBookable()).isEqualTo(true);
        assertThat(penultimatePeriod.getType()).isEqualTo(PeriodType.TEACHING);

        Period lastPeriod = periods.get(8);
        assertThat(lastPeriod.getFrom()).isEqualTo(parse("2021-05-30T15:30Z"));
        assertThat(lastPeriod.getTo()).isEqualTo(parse("2021-05-30T16:00Z"));
        assertThat(lastPeriod.isBookable()).isEqualTo(true);
        assertThat(lastPeriod.getType()).isEqualTo(PeriodType.TEACHING);
    }

    @Test
    public void GIVEN_if_the_end_time_is_not_a_full_period_AND_is_bookable_EXPECT_no_extra_period_is_created() {

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T12:35Z", true);
        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(2);

        assertIsValidCheckInPeriod(periods.get(0), "2021-05-30T11:45Z", "2021-05-30T12:00Z", true);

        Period onlyTeachingPeriod = periods.get(1);
        assertThat(onlyTeachingPeriod.getFrom()).isEqualTo(parse("2021-05-30T12:00Z"));
        assertThat(onlyTeachingPeriod.getTo()).isEqualTo(parse("2021-05-30T12:30Z"));
        assertThat(onlyTeachingPeriod.getType()).isEqualTo(PeriodType.TEACHING);
        assertThat(onlyTeachingPeriod.isBookable()).isEqualTo(true);
    }
    
    @Test
    public void GIVEN_if_the_end_time_is_not_a_full_period_AND_not_bookable_EXPECT_no_extra_period_is_created() {

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T12:35Z", false);
        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(2);

        assertIsValidCheckInPeriod(periods.get(0), "2021-05-30T11:45Z", "2021-05-30T12:00Z", false);

        Period onlyTeachingPeriod = periods.get(1);
        assertThat(onlyTeachingPeriod.getFrom()).isEqualTo(parse("2021-05-30T12:00Z"));
        assertThat(onlyTeachingPeriod.getTo()).isEqualTo(parse("2021-05-30T12:30Z"));
        assertThat(onlyTeachingPeriod.getType()).isEqualTo(PeriodType.TEACHING);
        assertThat(onlyTeachingPeriod.isBookable()).isEqualTo(false);
    }

    @Test
    public void GIVEN_availability_is_not_long_enough_to_cover_a_period_EXPECT_no_teaching_period_is_created() {
        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T12:15Z", false);
        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(1);

        assertIsValidCheckInPeriod(periods.get(0), "2021-05-30T11:45Z", "2021-05-30T12:00Z", false);
    }

    @Test
    public void GIVEN_if_availability_is_not_bookable_EXPECT_all_periods_should_be_unbookable() {
        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T16:00Z", false);
        availability.setBookable(false);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(9);

        Period checkinPeriod = periods.get(0);
        assertIsValidCheckInPeriod(checkinPeriod, "2021-05-30T11:45Z", "2021-05-30T12:00Z", false);

        Period firstPeriod = periods.get(1);
        assertThat(firstPeriod.getFrom()).isEqualTo(parse("2021-05-30T12:00Z"));
        assertThat(ChronoUnit.MINUTES.between(firstPeriod.getFrom(), firstPeriod.getTo())).isEqualTo(30);
        assertThat(firstPeriod.isBookable()).isEqualTo(false);
        assertThat(firstPeriod.getType()).isEqualTo(PeriodType.TEACHING);


        Period lastPeriod = periods.get(8);
        assertThat(lastPeriod.getFrom()).isEqualTo(parse("2021-05-30T15:30Z"));
        assertThat(lastPeriod.getTo()).isEqualTo(parse("2021-05-30T16:00Z"));
        assertThat(lastPeriod.getType()).isEqualTo(PeriodType.TEACHING);

        assertThat(periods.stream().noneMatch(Period::isBookable)).isEqualTo(true);
    }

    @Test
    public void GIVEN_valid_teaching_times_with_bookable_period_EXPECT_check_in_period_is_created() {

        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T14:30Z", true);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(6);

        Period checkinPeriod = periods.get(0);
        assertIsValidCheckInPeriod(checkinPeriod, "2021-05-30T11:45Z", "2021-05-30T12:00Z", true);
    }
    
    @Test
    public void GIVEN_valid_teaching_times_with_unbookable_period_EXPECT_check_in_period_is_created() {

        whenMockRepositoryIsGivenPeriodsThenJustReturnThem(_mockPeriodRepository);

        Availability availability = createTestAvailability("2021-05-30T12:00Z", "2021-05-30T14:30Z", false);

        List<Period> periods = _testService.createPeriods(availability);

        assertThat(periods).hasSize(6);

        Period checkinPeriod = periods.get(0);
        assertIsValidCheckInPeriod(checkinPeriod, "2021-05-30T11:45Z", "2021-05-30T12:00Z", false);
    }

    private void whenMockRepositoryIsGivenPeriodsThenJustReturnThem(IPeriodRepository mockPeriodRepository) {
        when(mockPeriodRepository.saveAll(notNull())).thenAnswer((Answer<List<Period>>) invocation -> invocation.getArgument(0));
    }

    private Availability createTestAvailability(String fromDate, String toDate, boolean isBookable) {
        return Availability
                .builder()
                .from(parse(fromDate))
                .to(parse(toDate))
                .isBookable(isBookable)
                .build();
    }

    private void assertIsValidCheckInPeriod(Period checkinPeriod, String fromTime, String toTime, boolean isBookable) {
        assertThat(checkinPeriod.getFrom()).isEqualTo(parse(fromTime));
        assertThat(checkinPeriod.getTo()).isEqualTo(parse(toTime));
        assertThat(checkinPeriod.isBookable()).isEqualTo(isBookable);
        assertThat(checkinPeriod.getType()).isEqualTo(PeriodType.CHECK_IN);
    }
}