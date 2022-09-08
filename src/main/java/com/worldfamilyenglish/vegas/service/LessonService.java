package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Lesson;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;
import com.worldfamilyenglish.vegas.persistence.ILessonRepository;
import com.worldfamilyenglish.vegas.persistence.IParentRepository;
import com.worldfamilyenglish.vegas.persistence.IPeriodRepository;
import com.worldfamilyenglish.vegas.persistence.ISeatRepository;
import com.worldfamilyenglish.vegas.service.zoom.IZoomService;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LessonService {

    private static final int DEFAULT_SEATS_PER_LESSON = 3;

    private final ILessonRepository _lessonRepository;
    private final ISeatRepository _seatRepository;

    private final IPeriodRepository _periodRepository;
    private final IZoomService _zoomService;

    @Autowired
    public LessonService(
    		final ILessonRepository lessonRepository,
    		final ISeatRepository seatRepository,
            final IPeriodRepository periodRepository,
    		final IZoomService zoomService) {
        _lessonRepository = lessonRepository;
        _seatRepository = seatRepository;
        _zoomService = zoomService;
        _periodRepository = periodRepository;
    }

    public synchronized List<Seat> findFreeSeats(final ContentInfo content, final OffsetDateTime lessonTime) {
        return _seatRepository.findByContent_IdAndStartTimeAndBookingNull(content.getId(), lessonTime);
    }


    public synchronized Seat createLessonAndSeat(final ContentInfo contentInfo, final Period period) {

    	String zoomMeetingUrl = null;

    	try {
    		zoomMeetingUrl = _zoomService.createMeeting(contentInfo, period);
    	} catch (Exception exception) {
    		LOG.error("lesson can't be created due to exception thrown while creating zoom meeting.");
    		throw exception;
		}

        Lesson lesson = Lesson
                .builder()
                .content(contentInfo)
                .period(period)
                .zoomMeetingUrl(zoomMeetingUrl)
                .build();


        for (int i = 0; i < DEFAULT_SEATS_PER_LESSON; i++) {
            Seat seat = Seat
                    .builder()
                    .content(contentInfo)
                    .startTime(period.getFrom())
                    .build();

            lesson.addSeat(seat);
        }

        lesson = _lessonRepository.save(lesson);

        period.setLesson(lesson);

        return lesson.getSeats().get(0);
    }

    public List<Seat> findSeats(final ContentInfo publishedContent, final OffsetDateTime from, final OffsetDateTime to) {
        return _seatRepository.findByContentAndStartTimeGreaterThanEqualAndStartTimeLessThanEqual(publishedContent, from, to);
    }

    public synchronized void cancelSeatBooking(final Seat seat) {
        seat.clearBooking();

        _seatRepository.save(seat);
        _seatRepository.flush();

        Optional<Lesson> foundLesson = _lessonRepository.findById(seat.getLesson().getId());

        Lesson lesson = foundLesson.get();
        boolean hasAnyBookings = lesson.hasAnyBookings();

        if (!hasAnyBookings) {
            LOG.info("Lesson {} does not have any bookings, destroy and freeing up the period.", lesson);
            destroyLesson(lesson);
        }
    }

    private void destroyLesson(final Lesson lesson) {
        // TODO: in theory we should destroy the Zoom meeting, but
        // in future we should only create Zoom lessons once there's no chance to cancel the lesson:
        // https://wfeinnovation.atlassian.net/jira/software/projects/VEGAS/boards/75?selectedIssue=VEGAS-79
        //
        Period period = lesson.getPeriod();
        period.clearLesson();
        _periodRepository.save(period);
        _lessonRepository.deleteById(lesson.getId());

    }
}
