package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.service.BookabilityService;
import com.worldfamilyenglish.vegas.types.Bookability;
import com.worldfamilyenglish.vegas.types.BookabilityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@DgsComponent
@Slf4j
public class BookabilityFetcher {

    public static final int DEFAULT_SEARCH_DAYS_WINDOW = 7;
    private BookabilityService _bookabilityService;

    @Autowired
    public BookabilityFetcher(final BookabilityService bookabilityService) {
        _bookabilityService = bookabilityService;
    }

    @DgsQuery
    public List<Bookability> bookability(@InputArgument("filter") final BookabilityFilter filter) {
        final OffsetDateTime defaultFrom  = OffsetDateTime.now();
        final OffsetDateTime defaultTo = defaultFrom.plusDays(DEFAULT_SEARCH_DAYS_WINDOW);

        List<Bookability> foundBookability;

        if (filter == null) {
            foundBookability = _bookabilityService.findBookability(defaultFrom, defaultTo);
        } else {
            final OffsetDateTime from = filter.getFrom() == null ? defaultFrom : filter.getFrom();
            final OffsetDateTime to = filter.getTo() == null ? defaultTo : filter.getTo();

            if (filter.getCapLevel() == null) {
                LOG.debug("Finding bookability of seats, periods between {} and {}, for all CAP Levels", from, to);
                foundBookability =  _bookabilityService.findBookability(from, to);
            } else {
                LOG.debug("Finding bookability of seats, periods between {} and {}, for CAP Level {}", from, to, filter.getCapLevel());
                foundBookability = _bookabilityService.findBookability(filter.getCapLevel(), from, to);
            }
        }

        return foundBookability;
    }
}
