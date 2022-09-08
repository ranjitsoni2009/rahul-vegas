package com.worldfamilyenglish.vegas.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.worldfamilyenglish.vegas.domain.Parent;

public interface IParentRepository extends JpaRepository<Parent, Long> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Parent> findById(final Long id);

    Optional<Parent> findByExternalId(final String externalId);
}
