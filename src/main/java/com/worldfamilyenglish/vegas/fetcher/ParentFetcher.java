package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.DgsConstants;
import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.service.ParentService;
import com.worldfamilyenglish.vegas.types.ParentInfoFilter;
import com.worldfamilyenglish.vegas.util.ParentUtilities;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@DgsComponent
@Slf4j
public class ParentFetcher {

    private final ParentService _parentService;

    public ParentFetcher(final ParentService parentService) {
        _parentService = parentService;
    }

    @DgsQuery
    public Parent parentInfo(@InputArgument("filter") final ParentInfoFilter filter) {
        if (filter == null) {
            return null;
        }

        return _parentService.getParent(filter.getExternalId()).orElse(null);
    }

    @DgsData(parentType = DgsConstants.PARENT.TYPE_NAME, field = DgsConstants.PARENT.Bookings)
    public List<Booking> bookingsForChildren(final DataFetchingEnvironment dataFetchingEnvironment) {
        Parent parent = dataFetchingEnvironment.getSource();

        return ParentUtilities.getBookingsFromChildren(parent);
    }
}
