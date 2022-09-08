package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.domain.Seat;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ISeatRepository extends JpaRepository<Seat, Long> {

    @NotNull
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Seat> findById(@NotNull final Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Seat> findByContent_IdAndStartTimeAndBookingNull(final Long id, final OffsetDateTime startTime);

    List<Seat> findByContentAndStartTimeGreaterThanEqualAndStartTimeLessThanEqual(final ContentInfo content, final OffsetDateTime queryFrom, final OffsetDateTime queryTo);


}
