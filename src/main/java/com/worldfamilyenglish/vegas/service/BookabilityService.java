package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.*;
import com.worldfamilyenglish.vegas.types.Bookability;
import com.worldfamilyenglish.vegas.types.BookablePeriod;
import com.worldfamilyenglish.vegas.util.BookabilityUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookabilityService {

    private final ContentService _contentService;
    private final LessonService _lessonService;
    private final PeriodService _periodService;

    @Autowired
    public BookabilityService(final ContentService contentService, final LessonService lessonService, final PeriodService periodService) {
        _contentService = contentService;
        _lessonService = lessonService;
        _periodService = periodService;
    }

    public List<Bookability> findBookability(final CapLevel capLevel, final OffsetDateTime from, final OffsetDateTime to) {

        final List<ContentPublishingPlan> allPublishingPlans = _contentService.getAllPublishingPlans(from, to);
        final List<ContentPublishingPlan> filteredPlans =
                allPublishingPlans
                .stream()
                .filter(plan -> plan.getContent()
                        .getLevel()
                        .equals(capLevel))
                .toList();

        return getBookabilities(from, to, filteredPlans);
    }

    public List<Bookability> findBookability(final OffsetDateTime from, final OffsetDateTime to) {
        List<ContentPublishingPlan> allPublishingPlans = _contentService.getAllPublishingPlans(from, to);

        return getBookabilities(from, to, allPublishingPlans);
    }

    private List<Bookability> getBookabilities(final OffsetDateTime from, final OffsetDateTime to, final List<ContentPublishingPlan> allPublishingPlans) {
        final List<Bookability> bookabilities = new ArrayList<>();

        for (ContentPublishingPlan plan : allPublishingPlans) {
            final ContentInfo publishedContent = plan.getContent();
            final List<Seat> seatsForContent = _lessonService.findSeats(publishedContent, from, to);
            final List<Period> periods = _periodService.findBookablePeriodsForContent(publishedContent, from, to);

            final List<BookablePeriod> bookablePeriods = BookabilityUtilities.buildBookablePeriods(seatsForContent, periods);

            final Bookability bookability = Bookability
                    .newBuilder()
                    .content(publishedContent)
                    .bookablePeriods(bookablePeriods)
                    .build();

            bookabilities.add(bookability);
        }

        return bookabilities;
    }
}
