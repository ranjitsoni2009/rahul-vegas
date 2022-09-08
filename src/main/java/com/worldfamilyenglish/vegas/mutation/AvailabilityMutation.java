package com.worldfamilyenglish.vegas.mutation;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.service.AvailabilityService;
import com.worldfamilyenglish.vegas.types.CreateAvailabilityInput;
import graphql.com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

@DgsComponent
public class AvailabilityMutation {

    private final AvailabilityService _availabilityService;


    @Autowired
    public AvailabilityMutation(final AvailabilityService availabilityService) {
        _availabilityService = availabilityService;
    }

    @DgsMutation
    public Availability createAvailability(@NotNull @InputArgument("input") final CreateAvailabilityInput input) {
        if (input == null) {
            throw new IllegalArgumentException("input parameter cannot be null");
        }

        Availability availability = Availability
                .builder()
                .from(input.getFrom())
                .to(input.getTo())
                .isBookable(input.getIsBookable() != null && input.getIsBookable()).build();

        return _availabilityService.create(availability, input.getOwnerId());
    }
}
