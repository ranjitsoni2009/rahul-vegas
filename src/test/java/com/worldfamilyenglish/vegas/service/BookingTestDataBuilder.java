package com.worldfamilyenglish.vegas.service;

import java.time.OffsetDateTime;

import com.worldfamilyenglish.vegas.domain.Booking;
import com.worldfamilyenglish.vegas.domain.BookingStatus;
import com.worldfamilyenglish.vegas.domain.CapLevel;
import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.domain.Lesson;
import com.worldfamilyenglish.vegas.domain.Period;
import com.worldfamilyenglish.vegas.domain.Seat;

public class BookingTestDataBuilder {

	public static ContentInfo getContentInfo() {

		return ContentInfo.builder()
				.name("name")
				.description("description")
				.build();
	}

	public static Child getChildInfo() {

		return Child.builder()
				.capLevel(CapLevel.GREEN)
				.externalId("externalId")
				.build();
	}

	public static DomainUser getDomainUser() {

		return DomainUser.builder()
				.givenName("GivenName")
				.familyName("FamilyName")
				.email("teacher@wfe.com").build();
	}

	public static Period getPeriod(DomainUser domainUser) {

		OffsetDateTime from = OffsetDateTime.parse("2022-08-21T07:30:55Z");
		OffsetDateTime to = OffsetDateTime.parse("2022-08-21T08:30:55Z");

		return Period.builder()
				.owner(domainUser)
				.from(from)
				.to(to)
				.build();
	}

	public static Lesson getLesson(ContentInfo contentInfo, Period period) {

		return Lesson.builder()
				.content(contentInfo)
				.period(period)
				.build();
	}

	public static Seat getSeat(ContentInfo contentInfo, Lesson lesson) {

		return Seat.builder()
				.content(contentInfo)
				.lesson(lesson)
				.build();
	}

	public static Booking getNewBooking(BookingStatus bookingStatus, Child child, Seat seat) {

		return Booking.builder()
				.status(bookingStatus)
				.child(child)
				.seat(seat)
				.build();
	}

}
