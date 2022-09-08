package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface IChildRepository extends JpaRepository<Child, Long> {
//    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Optional<Child> findByExternalId(final String externalChildId);
}
