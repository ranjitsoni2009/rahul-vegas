package com.worldfamilyenglish.vegas.service;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.persistence.IAvailabilityRepository;
import com.worldfamilyenglish.vegas.persistence.IDomainUserRepository;
import com.worldfamilyenglish.vegas.util.DomainUserUtilities;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DomainUserServiceImpl implements IDomainUserService {

    private final IDomainUserRepository _domainUserRepository;
    private final IAvailabilityRepository _availabilityRepository;

    @Autowired
    public DomainUserServiceImpl(final IDomainUserRepository domainUserRepository, final IAvailabilityRepository availabilityRepository) {
        _domainUserRepository = domainUserRepository;
        _availabilityRepository = availabilityRepository;
    }

    @Override
    public Set<DomainUser> allUsers() {
        List<DomainUser> allDomainUsers = _domainUserRepository.findAll();
        return Set.copyOf(allDomainUsers);
    }

    @Override
    public Optional<DomainUser> getUser(final Long id) {
        return _domainUserRepository.findById(id);
    }

    @Override
    public Optional<DomainUser> getUserByEmail(final String email) {
        return _domainUserRepository.findByEmail(email);
    }

    @Override
    public DomainUser createUser(final DomainUser newDomainUser) {

        final String primaryEmail = newDomainUser.getEmail().toLowerCase();

        newDomainUser.setEmail(primaryEmail);

        final Optional<DomainUser> foundUserByEmail = getUserByEmail(primaryEmail);

        if (foundUserByEmail.isPresent()) {
            throw new IllegalArgumentException(String.format("User already exists with same email address: '%s'.", primaryEmail));
        }

        return _domainUserRepository.save(newDomainUser);
    }

    @Override
    public void updateUser(final DomainUser domainUser) {
        _domainUserRepository.save(domainUser);
    }

    @Override
    public DomainUser getOrCreateUser(final AadOAuth2AuthenticatedPrincipal principal) {

        final String primaryEmail = getEmail(principal);

        final Optional<DomainUser> foundDomainUser = getUserByEmail(primaryEmail);

        if (foundDomainUser.isPresent()) {
            return foundDomainUser.get();
        }

        final DomainUser user = createUser(principal);

        return _domainUserRepository.save(user);
    }

    @Override
    public DomainUser forAvailability(final Long availabilityId) {
        final Optional<Availability> foundAvailability = _availabilityRepository.findById(availabilityId);

        return foundAvailability.map(Availability::getOwner).orElse(null);
    }

    private DomainUser createUser(final AadOAuth2AuthenticatedPrincipal principal) {
        String fullName = (String) principal.getClaim("name");

        String[] names = DomainUserUtilities.parseName(fullName);

        String primaryEmail = getEmail(principal);

        return DomainUser
                .builder()
                .activeDirectoryId(principal.getName())

                .givenName(names[0])
                .familyName(names[1])

                .email(primaryEmail)

                .build();
    }


    @NotNull
    private String getEmail(final AadOAuth2AuthenticatedPrincipal principal) {
        JSONArray emails = (JSONArray) principal.getClaim("emails");
        String primaryEmail = (String) emails.get(0);
        primaryEmail = primaryEmail.toLowerCase();

        return primaryEmail;
    }
}
