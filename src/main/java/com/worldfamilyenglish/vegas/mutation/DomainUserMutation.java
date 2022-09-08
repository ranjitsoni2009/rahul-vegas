package com.worldfamilyenglish.vegas.mutation;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.service.IDomainUserService;
import com.worldfamilyenglish.vegas.types.CreateUserInput;
import com.worldfamilyenglish.vegas.types.UpdateUserInput;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@DgsComponent
public class DomainUserMutation {

    private final IDomainUserService _userService;

    @Autowired
    public DomainUserMutation(final IDomainUserService backOfficeUserService) {
        _userService = backOfficeUserService;
    }

    @DgsMutation
    public DomainUser createUser(@NotNull @InputArgument("input") final CreateUserInput input) {

        DomainUser newDomainUser = DomainUser
                .builder()
                .givenName(input.getGivenName())
                .familyName(input.getFamilyName())
                .email(input.getEmail())
                .canTeach(input.getCanTeach() != null && input.getCanTeach())
                .canAdmin(input.getCanAdmin() != null && input.getCanAdmin())
                .build();

        return _userService.createUser(newDomainUser);
    }

    @DgsMutation
    public DomainUser updateUser(@NotNull @InputArgument("input") final UpdateUserInput input) {

        Optional<DomainUser> originalUser = _userService.getUser((long) (input.getId()));
        if (originalUser.isEmpty()) {
            throw new IllegalArgumentException("User with id '%d' does not exist".formatted(input.getId()));
        }

        DomainUser domainUser = originalUser.get();
        domainUser.setCanAdmin(input.getCanAdmin() != null  && input.getCanAdmin());
        domainUser.setCanTeach(input.getCanTeach() != null && input.getCanTeach());

        _userService.updateUser(domainUser);

        return domainUser;
    }
}