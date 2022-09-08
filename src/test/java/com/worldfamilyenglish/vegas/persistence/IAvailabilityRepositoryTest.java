package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
@ActiveProfiles("test")
class IAvailabilityRepositoryTest {

    @Autowired
    private IAvailabilityRepository _testRepository;

    @Autowired
    private IDomainUserRepository _domainUserRepository;

    private DomainUser _domainUser;

    @BeforeEach
    void setUp() {
        _domainUser = _domainUserRepository.save(DomainUser.builder().build());
    }

    @Test
    public void GIVEN_availability_is_persisted_EXPECT_object_is_equals_when_loaded() {

        Availability availability = createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser);
        Availability persistedAvailability = _testRepository.save(availability);

        Long id = persistedAvailability.getId();

        Optional<Availability> foundAvailability = _testRepository.findById(id);

        assertThat(foundAvailability).isPresent();
        Availability testAvailability = foundAvailability.get();
        assertThat(testAvailability).isEqualTo(availability);

        assertThat(testAvailability.getFrom()).isEqualTo("2022-01-01T12:00:00Z");
        assertThat(testAvailability.getTo()).isEqualTo("2022-01-01T20:00:00Z");
    }


    @Test
    public void GIVEN_query_period_is_outside_of_availability_EXPECT_availability_can_be_found() {
        Availability availability = createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser);
        _testRepository.save(availability);

        OffsetDateTime midnight = OffsetDateTime.parse("2022-01-01T00:00:00Z");
        OffsetDateTime endOfJan = OffsetDateTime.parse("2022-01-31T23:59:00Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(midnight, endOfJan);

        assertThat(foundAvailabilities).hasSize(1);

        Availability foundAvailability = foundAvailabilities.get(0);
        assertThat(foundAvailability.getFrom()).isEqualTo(OffsetDateTime.parse("2022-01-01T12:00:00Z"));
        assertThat(foundAvailability.getTo()).isEqualTo(OffsetDateTime.parse("2022-01-01T20:00:00Z"));
    }

    @Test
    public void GIVEN_query_start_and_end_date_are_exactly_at_the_start_of_availability_EXPECT_availability_is_found() {
        List<Availability> availabilityDeclarations = List.of(
                createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser),
                createAvailability("2022-01-02T12:00:00Z", "2022-01-02T20:00:00Z", _domainUser)
        );

        _testRepository.saveAll(availabilityDeclarations);

        OffsetDateTime queryStart = OffsetDateTime.parse("2022-01-01T12:00:00Z");
        OffsetDateTime queryEnd = OffsetDateTime.parse("2022-01-01T20:00:00Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(queryStart, queryEnd);

        assertThat(foundAvailabilities).hasSize(1);

        Availability foundAvailability = foundAvailabilities.get(0);
        assertThat(foundAvailability.getFrom()).isEqualTo(OffsetDateTime.parse("2022-01-01T12:00:00Z"));
        assertThat(foundAvailability.getTo()).isEqualTo(OffsetDateTime.parse("2022-01-01T20:00:00Z"));
    }

    @Test
    public void GIVEN_query_start_and_end_date_are_within_availability_period_EXPECT_availability_is_found() {
        Availability availability = createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser);
        _testRepository.save(availability);

        OffsetDateTime fourOClock = OffsetDateTime.parse("2022-01-01T16:00:00Z");
        OffsetDateTime fiveOClock = OffsetDateTime.parse("2022-01-01T17:00:00Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(fourOClock, fiveOClock);

        assertThat(foundAvailabilities).hasSize(1);

        Availability foundAvailability = foundAvailabilities.get(0);
        assertThat(foundAvailability.getFrom()).isEqualTo(OffsetDateTime.parse("2022-01-01T12:00:00Z"));
        assertThat(foundAvailability.getTo()).isEqualTo(OffsetDateTime.parse("2022-01-01T20:00:00Z"));

    }

    @Test
    public void GIVEN_query_start_and_end_time_and_availability_time_are_all_the_same_EXPECT_availability_is_found() {
        Availability availability = createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser);
        _testRepository.save(availability);

        OffsetDateTime midday = OffsetDateTime.parse("2022-01-01T12:00:00Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(midday, midday);

        assertThat(foundAvailabilities).hasSize(1);

        Availability foundAvailability = foundAvailabilities.get(0);
        assertThat(foundAvailability.getFrom()).isEqualTo(OffsetDateTime.parse("2022-01-01T12:00:00Z"));
        assertThat(foundAvailability.getTo()).isEqualTo(OffsetDateTime.parse("2022-01-01T20:00:00Z"));
    }

    @Test
    public void GIVEN_query_period_does_not_overlap_with_availabilities_EXPECT_nothing_is_found() {
        Availability availability = createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser);
        _testRepository.save(availability);

        OffsetDateTime someTimeInAprilStart = OffsetDateTime.parse("2022-04-01T12:00:00Z");
        OffsetDateTime someTimeInAprilEnd = OffsetDateTime.parse("2022-04-30T12:00:00Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(someTimeInAprilStart, someTimeInAprilEnd);

        assertThat(foundAvailabilities).isEmpty();
    }

    @Test
    public void GIVEN_query_is_start_and_end_of_year_EXPECT_all_availabilities_in_year_long_query_period_are_returned() {
        List<Availability> availabilities = List.of(
                createAvailability("2022-01-01T12:00:00Z", "2022-01-01T20:00:00Z", _domainUser),
                createAvailability("2022-04-01T12:00:00Z", "2022-04-01T20:00:00Z", _domainUser),
                createAvailability("2022-04-30T12:00:00Z", "2022-04-30T20:00:00Z", _domainUser),
                createAvailability("2022-08-01T12:00:00Z", "2022-04-01T20:00:00Z", _domainUser)
        );

        _testRepository.saveAll(availabilities);

        OffsetDateTime newYearsDay = OffsetDateTime.parse("2022-01-01T00:00:00Z");
        OffsetDateTime newYearsEve = OffsetDateTime.parse("2022-12-31T23:59:59Z");
        List<Availability> foundAvailabilities = _testRepository.findByToGreaterThanEqualAndFromLessThanEqual(newYearsDay, newYearsEve);

        assertThat(foundAvailabilities).hasSize(4);
    }


    private Availability createAvailability(final String startTime, final String endTime, final DomainUser owner) {
        return Availability
                .builder()
                .from(OffsetDateTime.parse(startTime))
                .to(OffsetDateTime.parse(endTime))
                .isBookable(true)
                .owner(owner)
                .build();
    }



}