package com.worldfamilyenglish.vegas.mutation;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.service.AvailabilityService;
import com.worldfamilyenglish.vegas.types.CreateAvailabilityInput;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AvailabilityMutationTest {

    @Test
    public void GIVEN_bookable_value_on_input_is_missing_EXPECT_default_is_false() {
        AvailabilityService mockAvailabilityService = mock(AvailabilityService.class);

        AvailabilityMutation testMutation = new AvailabilityMutation(mockAvailabilityService);

        CreateAvailabilityInput input = CreateAvailabilityInput
                .newBuilder()
                .from(OffsetDateTime.parse("1999-01-01T00:00:00Z"))
                .to(OffsetDateTime.parse("2024-01-01T23:59:59Z"))
                .ownerId(1)
                .build();

        ArgumentCaptor<Availability> inputArgumentCaptor = ArgumentCaptor.forClass(Availability.class);

        testMutation.createAvailability(input);

        verify(mockAvailabilityService).create(inputArgumentCaptor.capture(), eq(1L));

        Availability availability = inputArgumentCaptor.getValue();

        assertThat(availability).isNotNull();
        assertThat(availability.isBookable()).isFalse();

    }


    @Test
    public void GIVEN_null_input_EXPECT_exception_is_thrown() {
        AvailabilityService mockAvailabilityService = mock(AvailabilityService.class);

        AvailabilityMutation testMutation = new AvailabilityMutation(mockAvailabilityService);

        assertThrows(IllegalArgumentException.class, () -> testMutation.createAvailability(null));
    }

}