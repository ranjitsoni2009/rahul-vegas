package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.persistence.IAvailabilityRepository;
import com.worldfamilyenglish.vegas.persistence.IDomainUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.configuration.GlobalConfiguration.validate;

class AvailabilityServiceTest {


    private IAvailabilityRepository _mockAvailabilityRepository;
    private IDomainUserRepository _mockUserRepository;
    private AvailabilityService _testService;

    private PeriodService _mockPeriodService;

    @BeforeEach
    void setUp() {

        _mockAvailabilityRepository = mock(IAvailabilityRepository.class);
        _mockUserRepository = mock(IDomainUserRepository.class);
        _mockPeriodService = mock(PeriodService.class);

        _testService = new AvailabilityService(_mockAvailabilityRepository, _mockUserRepository, _mockPeriodService);
    }

    @Test
    public void GIVEN_availability_from_time_is_AFTER_to_time_EXPECT_exception_is_thrown() {

        Availability availability = Availability
                .builder()
                .from(OffsetDateTime.parse("2050-01-01T00:00:00Z"))
                .to(OffsetDateTime.parse("1999-01-01T00:00:00Z")).build();

        String exceptionMessage = assertThrows(IllegalArgumentException.class, () -> _testService.create(availability, 0)).getMessage();

        assertThat(exceptionMessage).isEqualTo("Availability from time (2050-01-01T00:00Z) is after to time (1999-01-01T00:00Z).");
    }

    @Test
    public void GIVEN_no_user_can_found_with_provided_id_EXPECT_exception_is_thrown() {

        when(_mockUserRepository.findById(0L)).thenReturn(Optional.empty());

        Availability pendingAvailability = Availability
                .builder()
                .from(OffsetDateTime.parse("2022-01-01T08:00Z"))
                .to(OffsetDateTime.parse("2022-01-01T12:30Z"))
                .build();

        String exceptionMessage = assertThrows(IllegalArgumentException.class, () -> _testService.create(pendingAvailability, 0)).getMessage();

        assertThat(exceptionMessage).isEqualTo("Could not find domain user with id '0'");

    }

    @Test
    public void GIVEN_availability_has_valid_ranges_EXPECT_ranges_are_passed_period_service() {

        DomainUser domainUser = DomainUser.builder()
                .id(1L)
                .givenName("Laura")
                .familyName("Performer")
                .build();


        when(_mockUserRepository.findById(1L)).thenReturn(Optional.of(domainUser));

        Availability pendingAvailability = Availability
                .builder()
                .from(OffsetDateTime.parse("2022-01-01T08:00Z"))
                .to(OffsetDateTime.parse("2022-01-01T12:30Z"))
                .build();

        _testService.create(pendingAvailability, 1);

        ArgumentCaptor<Availability> availabilityArgumentCaptor = ArgumentCaptor.forClass(Availability.class);

        verify(_mockPeriodService).createPeriods(availabilityArgumentCaptor.capture());

        assertThat(availabilityArgumentCaptor.getValue()).isNotNull();

        Availability availabilityParameter = availabilityArgumentCaptor.getValue();

        assertThat(availabilityParameter.getFrom()).isEqualTo("2022-01-01T08:00Z");
        assertThat(availabilityParameter.getTo()).isEqualTo("2022-01-01T12:30Z");
    }

    @Test
    public void GIVEN_from_and_to_are_over_12_hours_apart_EXPECT_illegal_argument_exception() {

        DomainUser domainUser = DomainUser.builder()
                .id(1L)
                .givenName("Laura")
                .familyName("Performer")
                .build();

        when(_mockUserRepository.findById(1L)).thenReturn(Optional.of(domainUser));

        Availability pendingAvailability = Availability
                .builder()
                .from(OffsetDateTime.parse("2022-01-01T08:00Z"))
                .to(OffsetDateTime.parse("2022-01-01T22:30Z"))
                .build();

        String errorMessage = assertThrows(IllegalArgumentException.class, () -> _testService.create(pendingAvailability, 1)).getMessage();

        assertThat(errorMessage).isEqualTo("Availability shift length is greater than 12 hours (14 hours), rejecting.");
    }


}