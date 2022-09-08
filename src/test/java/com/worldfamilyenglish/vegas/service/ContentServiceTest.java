package com.worldfamilyenglish.vegas.service;

import com.worldfamilyenglish.vegas.persistence.IContentInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentServiceTest {



    @BeforeEach
    void setUp() {

    }

    @Test
    public void GIVEN_the_content_cannot_be_found_EXPECT_an_exception_is_thrown() {
        IContentInfoRepository mockContentInfoRepository = mock(IContentInfoRepository.class);
        ContentService testService = new ContentService(null, mockContentInfoRepository, null, null);

        when(mockContentInfoRepository.findById(999L)).thenReturn(Optional.empty());

        long contentThatDoesNotExist = 999L;
        String errorMessage = assertThrows(IllegalArgumentException.class,
                () -> testService.getContentInfo(contentThatDoesNotExist)).getMessage();


        assertThat(errorMessage).isEqualTo("Could not find any content with id '999', is that a valid content id?");
    }


}