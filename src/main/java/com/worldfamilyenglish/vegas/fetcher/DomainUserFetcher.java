package com.worldfamilyenglish.vegas.fetcher;

import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.DgsConstants;
import com.worldfamilyenglish.vegas.domain.Availability;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.service.IDomainUserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@DgsComponent
@Slf4j
public class DomainUserFetcher {

    private final IDomainUserService _userService;

    public DomainUserFetcher(final IDomainUserService userService) {
        _userService = userService;
    }

    @DgsQuery
    public List<DomainUser> users(@InputArgument("id") final Integer id) {
        if (id == null) {
            return _userService.allUsers().stream().toList();
        }

        Optional<DomainUser> foundUser = _userService.getUser(id.longValue());

        return foundUser
                .map(List::of)
                .orElse(Collections.emptyList());
    }


    @DgsData(parentType = DgsConstants.AVAILABILITY.TYPE_NAME, field = DgsConstants.AVAILABILITY.Owner)
    public DomainUser owner(final DataFetchingEnvironment environment) {
        Availability availability = environment.getSource();

        return _userService.forAvailability(availability.getId());
    }


    @DgsQuery
    public DomainUser me() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AadOAuth2AuthenticatedPrincipal activeDirectoryPrincipal) {
            LOG.info("Getting credentials for user " + activeDirectoryPrincipal.getAttribute("name"));
            return _userService.getOrCreateUser(activeDirectoryPrincipal);
        } else {
            return null;
        }
    }
}
