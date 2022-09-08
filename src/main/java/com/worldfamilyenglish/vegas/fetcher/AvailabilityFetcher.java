package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.worldfamilyenglish.vegas.DgsConstants;
import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.persistence.IAvailabilityRepository;
import com.worldfamilyenglish.vegas.service.AvailabilityService;
import com.worldfamilyenglish.vegas.types.QueryAvailabilityFilter;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.List;

import static java.time.OffsetDateTime.*;

@DgsComponent
public class AvailabilityFetcher {
    private static final int DEFAULT_AVAILABILITY_FROM_DAYS = 7;
    private static final int DEFAULT_AVAILABILITY_TO_DAYS = 14;

    private final AvailabilityService _availabilityService;


    @Autowired
    public AvailabilityFetcher(final AvailabilityService availabilityService) {
        _availabilityService = availabilityService;
    }

    @DgsQuery
    public List<Availability> availability(final QueryAvailabilityFilter filter) {
        OffsetDateTime from = (filter != null) ? filter.getFrom() : now().minusDays(DEFAULT_AVAILABILITY_FROM_DAYS);
        OffsetDateTime to = (filter != null) ? filter.getTo() : now().plusDays(DEFAULT_AVAILABILITY_TO_DAYS);

        return _availabilityService.getRange(from, to);
    }

    @DgsData(parentType = DgsConstants.DOMAINUSER.TYPE_NAME, field = DgsConstants.DOMAINUSER.Availability)
    public List<Availability> availabilityForUsers(final DataFetchingEnvironment dataFetchingEnvironment) {
        DomainUser parentUser = dataFetchingEnvironment.getSource();

        OffsetDateTime from = now().minusDays(DEFAULT_AVAILABILITY_FROM_DAYS);
        OffsetDateTime to = now().plusDays(DEFAULT_AVAILABILITY_TO_DAYS);

        return _availabilityService.getRange(from, to, parentUser);
    }
}
