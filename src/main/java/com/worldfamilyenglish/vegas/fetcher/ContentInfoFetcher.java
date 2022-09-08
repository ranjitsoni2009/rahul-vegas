package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DgsComponent
public class ContentInfoFetcher {

    private final ContentService _contentService;

    @Autowired
    public ContentInfoFetcher(final ContentService contentService) {
        _contentService = contentService;
    }


    @DgsQuery
    public List<ContentInfo> contentInfos() {
        return _contentService.getAllContent();
    }
}
