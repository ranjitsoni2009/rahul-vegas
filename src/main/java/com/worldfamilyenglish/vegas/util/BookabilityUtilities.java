package com.worldfamilyenglish.vegas.util;

import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;
import com.worldfamilyenglish.vegas.types.BookablePeriod;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class BookabilityUtilities {

    private BookabilityUtilities() {
        // utility class not for instantiation
    }

    public static List<BookablePeriod> buildBookablePeriods(final List<Seat> seatsForContent, final List<Period> periods) {
        final HashMap<OffsetDateTime, SeatCount> timeToSeatCountMap = new HashMap<>();

        for (Seat seat : seatsForContent) {
            final SeatCount seatCount = getSeatCountForTime(timeToSeatCountMap, seat.getStartTime());

            if (seat.hasBooking()) {
                seatCount.bookedSeatCount ++;
            } else {
                seatCount.freeSeatCount ++;
            }
        }

        for (Period bookablePeriod : periods) {
            SeatCount seatCount = getSeatCountForTime(timeToSeatCountMap, bookablePeriod.getFrom());
            seatCount.periodCount ++;
        }

        return convertSeatCountToBookablePeriod(timeToSeatCountMap);
    }

    private static List<BookablePeriod> convertSeatCountToBookablePeriod(final HashMap<OffsetDateTime, SeatCount> timeToSeatCount) {
        return timeToSeatCount
                .entrySet()
                .stream()
                .map(entry -> BookablePeriod.newBuilder()
                                .from(entry.getKey())
                                .freePeriodsCount(entry.getValue().periodCount)
                                .freeSeatsCount(entry.getValue().freeSeatCount)
                                .bookedSeatsCount(entry.getValue().bookedSeatCount)
                                .build())
                .sorted(Comparator.comparing(BookablePeriod::getFrom))
                .toList();
    }

    private static SeatCount getSeatCountForTime(final HashMap<OffsetDateTime, SeatCount> timeToSeatCount, final OffsetDateTime key) {
        timeToSeatCount.putIfAbsent(key, new SeatCount());
        return timeToSeatCount.get(key);
    }

    private static class SeatCount {
        int freeSeatCount;
        int bookedSeatCount;
        int periodCount;
    }
}
