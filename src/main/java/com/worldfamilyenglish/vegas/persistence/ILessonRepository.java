package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Lesson;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ILessonRepository extends JpaRepository<Lesson, Long> {

    @NotNull
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lesson> findById(@NotNull final Long id);
}
