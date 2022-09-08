package com.worldfamilyenglish.vegas.mutation;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.service.BookingServiceFacade;
import com.worldfamilyenglish.vegas.service.ParentService;
import com.worldfamilyenglish.vegas.types.CreateBookingInput;

@DgsComponent
public class BookingMutation {

    private final BookingServiceFacade _bookingServiceFacade;

    private final ParentService _parentService;

    @Autowired
    public BookingMutation(final BookingServiceFacade bookingServiceFacade, final ParentService parentService) {
        _bookingServiceFacade = bookingServiceFacade;
        _parentService = parentService;
    }

    @DgsMutation(field = "createBooking")
    public Booking createBooking(@NotNull @InputArgument("input") final CreateBookingInput input) {

        if (input == null) {
            throw new IllegalArgumentException("input parameter cannot be null");
        }

        final List<Child> foundChildren = _parentService.getAssociatedChildren(input.getExternalChildId(), input.getCreateParentInput());

        return _bookingServiceFacade.createBooking(input.getExternalChildId(), (long) input.getContentId(), input.getLessonTime(),
                input.getTimeoutMinutes(), foundChildren);
    }

}
