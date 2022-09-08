package com.worldfamilyenglish.vegas.persistence;

import com.worldfamilyenglish.vegas.domain.DomainUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IDomainUserRepository extends JpaRepository<DomainUser, Long> {

    Optional<DomainUser> findByActiveDirectoryId(final String activeDirectoryId);

    Optional<DomainUser> findByEmail(final String emailAddress);
}
