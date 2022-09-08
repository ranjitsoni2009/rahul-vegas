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
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
@ActiveProfiles("test")
class IPeriodRepositoryTest {

    @Autowired
    private IPeriodRepository _periodRepository;

    @Autowired
    private ILessonRepository _lessonRepository;

    @Autowired
    private IDomainUserRepository _domainUserRepository;

    @Autowired
    private IContentPerformingAccreditationRepository _contentPerformingAccreditationRepository;

    @Autowired
    private IContentInfoRepository _contentInfoRepository;

    private DomainUser _testTeacher;

    private DomainUser _teacherWithNoSkills;
    private ContentInfo _testContent;


    @BeforeEach
    void setUp() {
        ContentInfo contentInfo = ContentInfo.builder().name("Blue Content").level(CapLevel.BLUE).externalUrl("http://example.com/content.pdf").build();
        DomainUser domainUser = DomainUser.builder().email("teacher@wfe.com").canTeach(true).build();
        _testTeacher = _domainUserRepository.save(domainUser);
        _testContent = _contentInfoRepository.save(contentInfo);
        _teacherWithNoSkills = _domainUserRepository.save(DomainUser.builder().email("newbie@wfe.com").canTeach(true).build());

        whenTeacherIsCreditedToTeach(_testContent, _testTeacher);
    }

    @Test
    public void GIVEN_a_bookable_period_and_the_teacher_can_teach_the_content_EXPECT_the_period_is_returned() {

        OffsetDateTime teachingTime = OffsetDateTime.parse("2022-04-15T13:00Z");

        Period period = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(teachingTime)
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.save(period);

        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(teachingTime, _testContent);

        assertThat(foundPeriods).hasSize(1);
    }

    @Test
    public void GIVEN_a_period_is_not_bookable_but_is_free_with_no_lesson_EXPECT_its_not_returned() {
        OffsetDateTime teachingTime = OffsetDateTime.parse("2022-04-15T13:00Z");

        Period period = Period.builder()
                .owner(_testTeacher)
                .isBookable(false)
                .from(teachingTime)
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.save(period);

        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(teachingTime, _testContent);

        assertThat(foundPeriods).isEmpty();
    }

    @Test
    public void GIVEN_a_period_is_bookable_but_already_has_a_lesson_EXPECT_its_not_returned() {
        OffsetDateTime teachingTime = OffsetDateTime.parse("2022-04-15T13:00Z");

        Period period = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(teachingTime)
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();


        _periodRepository.save(period);

        Lesson lesson = Lesson.builder().content(_testContent).build();


        period.setLesson(lesson);
        lesson.setPeriod(period);

        _lessonRepository.save(lesson);

        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(teachingTime, _testContent);

        assertThat(foundPeriods).isEmpty();
    }

    @Test
    public void GIVEN_the_time_is_wrong_EXPECT_the_period_is_not_returned() {

        Period period = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.save(period);

        OffsetDateTime incorrectTime = OffsetDateTime.parse("2022-04-15T14:00Z");
        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(incorrectTime, _testContent);

        assertThat(foundPeriods).isEmpty();
    }

    @Test
    public void GIVEN_a_period_is_bookable_AND_has_no_lesson_BUT_the_teacher_has_no_content_skills_EXPECT_the_period_is_not_returned() {

        Period period = Period.builder()
                .owner(_teacherWithNoSkills)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-08-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-08-15T13:30Z"))
                .build();

        _periodRepository.save(period);

        OffsetDateTime lessonTime = OffsetDateTime.parse("2022-08-15T13:00Z");
        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(lessonTime, _testContent);

        assertThat(foundPeriods).isEmpty();
    }

    @Test
    public void GIVEN_multiple_periods_match_EXPECT_all_of_them_are_returned() {
        whenTeacherIsCreditedToTeach(_testContent, _teacherWithNoSkills);

        Period testTeacherPeriod = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        Period nowSkilledTeacherPeriod = Period.builder()
                .owner(_teacherWithNoSkills)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.saveAll(List.of(testTeacherPeriod, nowSkilledTeacherPeriod));

        OffsetDateTime lessonTime = OffsetDateTime.parse("2022-04-15T13:00Z");
        List<Period> foundPeriods = _periodRepository.findByFromAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(lessonTime, _testContent);

        assertThat(foundPeriods).hasSize(2);
        assertThat(foundPeriods).extracting("owner").contains(_testTeacher, _teacherWithNoSkills);
    }

    @Test
    public void GIVEN_a_period_is_bookable_and_free_but_the_teacher_does_not_have_content_accreditation_EXPECT_the_period_is_not_found() {
        whenATeacherIsNOTcreditedToTeach(_testContent, _teacherWithNoSkills);

        Period testTeacherPeriod = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        Period zeroSkilledTeacherPeriod = Period.builder()
                .owner(_teacherWithNoSkills)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.saveAll(List.of(testTeacherPeriod, zeroSkilledTeacherPeriod));

        OffsetDateTime fromTime = OffsetDateTime.parse("2022-04-14T00:00Z");
        OffsetDateTime toTime = OffsetDateTime.parse("2022-04-17T00:00Z");

        List<Period> foundPeriods = _periodRepository.findByFromGreaterThanEqualAndToLessThanEqualAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(fromTime, toTime, _testContent);

        assertThat(foundPeriods).hasSize(1);
    }

    @Test
    public void GIVEN_a_period_is_bookable_and_free_AND_the_teacher_has_content_accreditation_EXPECT_the_period_is_found() {
        whenTeacherIsCreditedToTeach(_testContent, _teacherWithNoSkills);

        Period testTeacherPeriod = Period.builder()
                .owner(_testTeacher)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        Period zeroSkilledTeacherPeriod = Period.builder()
                .owner(_teacherWithNoSkills)
                .isBookable(true)
                .from(OffsetDateTime.parse("2022-04-15T13:00Z"))
                .to(OffsetDateTime.parse("2022-04-15T13:30Z"))
                .build();

        _periodRepository.saveAll(List.of(testTeacherPeriod, zeroSkilledTeacherPeriod));

        OffsetDateTime fromTime = OffsetDateTime.parse("2022-04-14T00:00Z");
        OffsetDateTime toTime = OffsetDateTime.parse("2022-04-17T00:00Z");

        List<Period> foundPeriods = _periodRepository.findByFromGreaterThanEqualAndToLessThanEqualAndIsBookableTrueAndLessonNullAndOwner_ContentPerformingAccreditations_ContentInfo(fromTime, toTime, _testContent);

        assertThat(foundPeriods).hasSize(2);

    }

    private void whenTeacherIsCreditedToTeach(final ContentInfo content, final DomainUser teacher) {
        ContentPerformingAccreditation accreditation = ContentPerformingAccreditation.builder().contentInfo(content).domainUser(teacher).isAccredited(true).build();
        _contentPerformingAccreditationRepository.save(accreditation);
    }

    private void whenATeacherIsNOTcreditedToTeach(final ContentInfo testContent, final DomainUser teacherWithNoSkills) {
        // no-op
    }
}