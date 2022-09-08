package com.worldfamilyenglish.vegas.schedule;


import com.worldfamilyenglish.vegas.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
public class BookingTimeoutScheduler {

    private final BookingService _bookingService;

    @Autowired
    public BookingTimeoutScheduler(final BookingService bookingService) {
        _bookingService = bookingService;
    }

    @Scheduled(cron = "${scheduledTasks.timeoutBookings}")
    @SchedulerLock(name = "timeoutBookings")
    public void timeoutBookings() {
        LockAssert.assertLocked();

        LOG.debug("Running Scheduled Task: Timing out any pending bookings");
        _bookingService.timeoutPendingBookings(OffsetDateTime.now());
    }
}
