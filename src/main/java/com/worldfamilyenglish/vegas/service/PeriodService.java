package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.*;
import com.worldfamilyenglish.vegas.persistence.IPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PeriodService {

    private final int CHECK_IN_PERIOD_IN_MINUTES = 15;
    private final int LENGTH_OF_PERIOD_IN_MINUTES = 30;
    private final IPeriodRepository _periodRepository;

    @Autowired
    public PeriodService(final IPeriodRepository periodRepository) {
        _periodRepository = periodRepository;
    }
    List<Period> createPeriods(final Availability availability) {
        List<Period> periods = new ArrayList<Period>();

        OffsetDateTime availabilityFrom = availability.getFrom();
        OffsetDateTime checkInPeriodFrom = availabilityFrom.minusMinutes(CHECK_IN_PERIOD_IN_MINUTES);

        Period checkinPeriod = createCheckInPeriod(availability, checkInPeriodFrom, availabilityFrom);

        periods.add(checkinPeriod);

        OffsetDateTime periodFrom = availabilityFrom;
        OffsetDateTime periodTo = availabilityFrom.plusMinutes(LENGTH_OF_PERIOD_IN_MINUTES);

        while (!periodTo.isAfter(availability.getTo())) {
            Period teachingPeriod = createTeachingPeriod(availability, periodFrom, periodTo, availability.isBookable());

            periods.add(teachingPeriod);

            periodFrom = periodFrom.plusMinutes(LENGTH_OF_PERIOD_IN_MINUTES);
            periodTo = periodTo.plusMinutes(LENGTH_OF_PERIOD_IN_MINUTES);
        }

        return _periodRepository.saveAll(periods);
    }

    private Period createCheckInPeriod(final Availability availability, final OffsetDateTime from, final OffsetDateTime to) {
        return Period
                .builder()
                .from(from)
                .to(to)
                .isBookable(availability.isBookable())
                .type(PeriodType.CHECK_IN)
                .owner(availability.getOwner())
                .owningAvailability(availability)
                .build();
    }

    private Period createTeachingPeriod(final Availability availability, final OffsetDateTime from, final OffsetDateTime to, final boolean isBookable) {
        return Period
                .builder()
                .from(from)
                .to(to)
                .isBookable(isBookable)
                .type(PeriodType.TEACHING)
                .owner(availability.getOwner())
                .owningAvailability(availability)
                .build();
    }

    /**
     * Find all free periods at the given time, that can be used to teach the given contentInfo... i.e. a teacher has been accredited to teach content for that
     * time period.
     * @param lessonTime the start time of the period.
     * @param contentInfo the {@link ContentInfo} that is expected to be taught during the given period.
     * @return a list of found periods that match, may be empty.
     */
    public List<Period> findBookablePeriodsForContent(final OffsetDateTime lessonTime, final ContentInfo contentInfo) {
        return _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(lessonTime, contentInfo);
    }

    public List<Period> findBookablePeriodsForContent(final ContentInfo publishedContent, final OffsetDateTime from, final OffsetDateTime to) {
        return _periodRepository.findByFromGreaterThanEqualAndToLessThanEqualAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(from, to, publishedContent);
    }
}
