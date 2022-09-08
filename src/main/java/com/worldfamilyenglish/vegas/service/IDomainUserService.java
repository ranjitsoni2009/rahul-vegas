package com.worldfamilyenglish.vegas.service;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal;
import com.worldfamilyenglish.vegas.domain.DomainUser;

import java.util.Optional;
import java.util.Set;

public interface IDomainUserService {
    Set<DomainUser> allUsers();

    Optional<DomainUser> getUser(final Long id);

    Optional<DomainUser> getUserByEmail(String email);

    DomainUser createUser(final DomainUser newDomainUser);

    void updateUser(final DomainUser domainUser);

    DomainUser getOrCreateUser(final AadOAuth2AuthenticatedPrincipal principal);

    DomainUser forAvailability(final Long availabilityId);
}
