package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.domain.DomainUser;
import com.worldfamilyenglish.vegas.persistence.IAvailabilityRepository;
import com.worldfamilyenglish.vegas.persistence.IDomainUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DomainUserServiceTest {

    private IDomainUserRepository _mockDomainUserRepository;
    private IAvailabilityRepository _mockAvailabilityRepository;

    private IDomainUserService _serviceUnderTest;

    @BeforeEach
    void setUp() {
        _mockDomainUserRepository = mock(IDomainUserRepository.class);
        _mockAvailabilityRepository = mock(IAvailabilityRepository.class);

        _serviceUnderTest = new DomainUserServiceImpl(_mockDomainUserRepository, _mockAvailabilityRepository);
    }

    @Test
    void GIVEN_multiple_users_exist_EXPECT_users_returned_from_service() {

        final DomainUser domainUser = getDomainUser();

        when(_mockDomainUserRepository.findAll()).thenReturn(List.of(domainUser));

        Set<DomainUser> returnedUsers = _serviceUnderTest.allUsers();

        assertThat(returnedUsers).isNotNull();
        assertThat(returnedUsers).isNotEmpty();
        assertThat(returnedUsers).hasSize(1);
    }

    @Test
    void GIVEN_user_exist_with_given_id_EXPECT_user_returned_from_service() {

        final DomainUser domainUser = getDomainUser();

        when(_mockDomainUserRepository.findById(1L)).thenReturn(Optional.of(domainUser));

        Optional<DomainUser> returnedUser = _serviceUnderTest.getUser(1L);

        assertThat(returnedUser).isPresent();
    }

    @Test
    void GIVEN_user_exist_with_given_email_EXPECT_user_returned_from_service() {

        final DomainUser domainUser = getDomainUser();

        when(_mockDomainUserRepository.findByEmail("test@service.com")).thenReturn(Optional.of(domainUser));

        Optional<DomainUser> returnedUser = _serviceUnderTest.getUserByEmail("test@service.com");

        assertThat(returnedUser).isPresent();
    }

    @Test
    void GIVEN_user_exist_with_given_email_EXPECT_service_throws_error_on_duplicate_creation() {

        final DomainUser domainUser = getDomainUser();

        when(_mockDomainUserRepository.findByEmail("test@service.com")).thenReturn(Optional.of(domainUser));

        String errorMessage = assertThrows(IllegalArgumentException.class, () -> _serviceUnderTest.createUser(domainUser)).getMessage();

        assertThat(errorMessage).isEqualTo("User already exists with same email address: 'test@service.com'.");
    }

    @Test
    void GIVEN_user_not_exist_with_given_email_EXPECT_user_created_by_service() {

        final DomainUser domainUser = getDomainUser();

        when(_mockDomainUserRepository.findByEmail("test@service.com")).thenReturn(Optional.empty());
        when(_mockDomainUserRepository.save(domainUser)).thenReturn(domainUser);

        DomainUser createdUser = _serviceUnderTest.createUser(domainUser);

        assertThat(createdUser).isNotNull();
    }

    @Test
    void GIVEN_user_updated_EXPECT_service_calls_repository() {

        final DomainUser domainUser = getDomainUser();

        _serviceUnderTest.updateUser(domainUser);

        verify(_mockDomainUserRepository, times(1)).save(any());
    }

    private DomainUser getDomainUser() {
        return DomainUser.builder()
            .id(1L)
            .email("test@service.com")
            .canTeach(true)
            .canAdmin(true)
            .activeDirectoryId("182738-siud8od-s9diud-d8dujd")
            .build();
    }
}
