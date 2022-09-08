package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.worldfamilyenglish.vegas.domain.ContentPerformingAccreditation;
import com.worldfamilyenglish.vegas.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DgsComponent
public class ContentPerformingAccreditationFetcher {

    private final ContentService _contentService;

    @Autowired
    public ContentPerformingAccreditationFetcher(final ContentService contentService) {
        _contentService = contentService;
    }

    @DgsQuery(field = "contentPerformingAccreditations")
    public List<ContentPerformingAccreditation> contentPerformingAccreditations() {
        return _contentService.getAllPerformingAccreditations();
    }
}
