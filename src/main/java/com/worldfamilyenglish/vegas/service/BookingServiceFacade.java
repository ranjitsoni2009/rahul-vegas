package com.worldfamilyenglish.vegas.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingServiceFacade {

    private final BookingService _bookingService;
    private final ContentService _contentService;
    private final Object _lockObject;


    @Autowired
    public BookingServiceFacade(final BookingService bookingService, final ContentService contentService) {
        _bookingService = bookingService;
        _contentService = contentService;

        _lockObject = new Object();
    }

    @Transactional
    public Booking createBooking(final String externalChildId, final Long contentId, final OffsetDateTime lessonTime, final Integer timeoutMinutes, final List<Child> children) {

    	ContentInfo foundContentInfo = _contentService.getContentInfo(contentId);
    	boolean isContentAvailable = _contentService.isContentPublishedDuringPeriod(lessonTime, foundContentInfo);

    	Booking booking = null;

    	synchronized (_lockObject) {
    		LOG.info("Booking lesson for child '{}' at time {}", externalChildId, lessonTime);
    		
    		Child child = getAssociatedChild(externalChildId, children);

    		if (!isContentAvailable) {
    			LOG.info("No content ({}) is available for the given time requested {}, returning unfulfillable.", contentId, lessonTime);
    			booking = _bookingService.createUnfulfillableBooking(child);
    		} else {
    			booking = _bookingService.createBooking(lessonTime, foundContentInfo, child, timeoutMinutes);

    			LOG.debug("...booking complete {}\n\n", booking);
    		}

    		List<Booking> existingBookings = child.getBookings();

    		if (existingBookings == null) {
    			existingBookings = new ArrayList<>();
    		}

    		existingBookings.add(booking);
    		child.setBookings(existingBookings);
    	}

    	return booking;
    }

    public void timeoutPendingBookings(final OffsetDateTime cutoffTime) {
        _bookingService.timeoutPendingBookings(cutoffTime);
    }
    
    private Child getAssociatedChild(final String externalChildId, final List<Child> children) {

    	Child foundChild = null;
    	Optional<Child> foundAssociatedChild =  children.stream().filter(child -> child.getExternalId().equals(externalChildId)).findFirst();
    	
    	if (foundAssociatedChild.isPresent()) {
    		foundChild = foundAssociatedChild.get();
    	}
    	else {
    		throw new IllegalArgumentException("Could not find the associated child for creating the booking for the provided externalChildId %s".formatted(externalChildId));
    	}
    	
    	return foundChild;
	}
}
