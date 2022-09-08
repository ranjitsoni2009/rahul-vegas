package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ISeatRepositoryTest {

    @Autowired
    private ISeatRepository _repoUnderTest;

    @Autowired
    private IContentInfoRepository _contentInfoRepository;

    @Autowired
    private ILessonRepository _lessonRepository;

    @Autowired
    private IPeriodRepository _periodRepository;

    @Autowired
    private IDomainUserRepository _domainUserRepository;

    private ContentInfo _testContent;
    private DomainUser _testUser;

    @BeforeEach
    void setUp() {

        _testUser = _domainUserRepository.save(DomainUser.builder().email("wendy@wfe.com").build());

        _testContent = ContentInfo.builder().name("Blue Test").level(CapLevel.BLUE).externalUrl("http://example.com/content.pdf").build();
        _testContent = _contentInfoRepository.save(_testContent);

        createTestSeatAndLesson("2022-04-01T14:00Z");

        List<Seat> testSeats = List.of(
                createTestSeatAndLesson("2022-04-01T14:00Z"),
                createTestSeatAndLesson("2022-04-01T14:00Z"),
                createTestSeatAndLesson("2022-04-15T14:00Z"),
                createTestSeatAndLesson("2022-04-30T14:00Z")
        );


        _repoUnderTest.saveAll(testSeats);
    }

    @Test
    public void GIVEN_a_large_search_window_EXPECT_all_seats_within_window_are_returned() {
        OffsetDateTime wideWindowFrom = OffsetDateTime.parse("2022-04-01T00:00Z");
        OffsetDateTime wideWindowTo = OffsetDateTime.parse("2022-04-30T23:59Z");

        List<Seat> foundSeats = _repoUnderTest.findByContentAndStartTimeGreaterThanEqualAndStartTimeLessThanEqual(_testContent, wideWindowFrom, wideWindowTo);

        assertThat(foundSeats).hasSize(4);
    }

    @Test
    public void GIVEN_a_small_search_window_EXPECT_no_seats_outside_window_are_returned() {
        OffsetDateTime wideWindowFrom = OffsetDateTime.parse("2022-04-14T00:00Z");
        OffsetDateTime wideWindowTo = OffsetDateTime.parse("2022-04-16T23:59Z");

        List<Seat> foundSeats = _repoUnderTest.findByContentAndStartTimeGreaterThanEqualAndStartTimeLessThanEqual(_testContent, wideWindowFrom, wideWindowTo);

        assertThat(foundSeats).hasSize(1);

    }

    private Seat createTestSeatAndLesson(final String lessonTime) {
        OffsetDateTime startTime = OffsetDateTime.parse(lessonTime);

        Period period = Period.builder().from(startTime).to(startTime.plusMinutes(30)).owner(_testUser).build();
        period = _periodRepository.save(period);

        Lesson lesson = Lesson.builder().content(_testContent).period(period).build();
        lesson = _lessonRepository.save(lesson);

        Seat seat = Seat
                .builder()
                .content(_testContent)
                .startTime(startTime)
                .lesson(lesson)
                .build();

        return seat;
    }
}