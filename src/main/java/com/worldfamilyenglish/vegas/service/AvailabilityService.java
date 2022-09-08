package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.persistence.IAvailabilityRepository;
import com.worldfamilyenglish.vegas.persistence.IDomainUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AvailabilityService {

    private static final int MAX_SHIFT_LENGTH_IN_HOURS = 12;
    private IAvailabilityRepository _availabilityRepository;
    private IDomainUserRepository _domainUserRepository;
    private PeriodService _periodService;

    @Autowired
    public AvailabilityService(final IAvailabilityRepository availabilityRepository, final IDomainUserRepository domainUserRepository, final PeriodService periodService) {
        _availabilityRepository = availabilityRepository;
        _domainUserRepository = domainUserRepository;
        _periodService = periodService;
    }

    @Transactional
    public Availability create(final Availability pendingAvailability, final long ownerId) {
        validateFromAndToOrder(pendingAvailability);
        validateAvailabilityDuration(pendingAvailability);

        DomainUser domainUser = findDomainUser(ownerId);
        pendingAvailability.setOwner(domainUser);

        boolean hasConflictingAvailability = checkAvailabilityConflict(pendingAvailability);

        if (hasConflictingAvailability) {
            // AS - July 2022 - In the future, set the availability as "CONFLICT" and save it.
            // don't create Periods
            throw new IllegalArgumentException("User %1$s already has availability declared from, %2$s to %3$s"
                    .formatted(domainUser.getEmail(), pendingAvailability.getFrom(), pendingAvailability.getTo()));
        }

        pendingAvailability.setPeriods(_periodService.createPeriods(pendingAvailability));

        LOG.info("Creating availability for user %1$s - %2$s : %3$s".formatted(domainUser.getEmail(), pendingAvailability.getFrom(), pendingAvailability.getTo()));

        return _availabilityRepository.save(pendingAvailability);
    }

    private boolean checkAvailabilityConflict(final Availability pendingAvailability) {
        List<Availability> overlappingAvailability = _availabilityRepository.findByToGreaterThanEqualAndFromLessThanEqualAndOwner(pendingAvailability.getFrom(), pendingAvailability.getTo(), pendingAvailability.getOwner());
        return !overlappingAvailability.isEmpty();
    }

    public List<Availability> getRange(final OffsetDateTime startTime, final OffsetDateTime endTime) {
        return _availabilityRepository.findByToGreaterThanEqualAndFromLessThanEqual(startTime, endTime);
    }

    /**
     * Get the range of declared availability for the given user.
     * @param startTime the start time to start looking
     * @param endTime the end time to start looking
     * @param owner the owner to look for
     * @return a non-null list of found availabilities.
     */
    public List<Availability> getRange(final OffsetDateTime startTime, final OffsetDateTime endTime, final DomainUser owner) {
        return _availabilityRepository.findByToGreaterThanEqualAndFromLessThanEqualAndOwner(startTime, endTime, owner);
    }

    private void validateFromAndToOrder(final Availability pendingAvailability) {
        if (!pendingAvailability.getTo().isAfter(pendingAvailability.getFrom())) {
            throw new IllegalArgumentException("Availability from time (%s) is after to time (%s).".formatted(pendingAvailability.getFrom(), pendingAvailability.getTo()));
        }
    }

    private void validateAvailabilityDuration(final Availability pendingAvailability) {
        long shiftDuration = ChronoUnit.HOURS.between(pendingAvailability.getFrom(), pendingAvailability.getTo());
        if (shiftDuration > MAX_SHIFT_LENGTH_IN_HOURS) {
            throw new IllegalArgumentException("Availability shift length is greater than %d hours (%d hours), rejecting.".formatted(MAX_SHIFT_LENGTH_IN_HOURS, shiftDuration));
        }
    }

    @NotNull
    private DomainUser findDomainUser(final long ownerId) {
        Optional<DomainUser> foundDomainUser = _domainUserRepository.findById(ownerId);

        if (foundDomainUser.isEmpty()) {
            throw new IllegalArgumentException("Could not find domain user with id '%d'".formatted(ownerId));
        }

        return foundDomainUser.get();
    }
}
