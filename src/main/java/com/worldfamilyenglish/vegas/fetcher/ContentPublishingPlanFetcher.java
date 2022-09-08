package com.worldfamilyenglish.vegas.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.worldfamilyenglish.vegas.domain.ContentPublishingPlan;
import com.worldfamilyenglish.vegas.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DgsComponent
public class ContentPublishingPlanFetcher {

    private final ContentService _contentService;

    @Autowired
    public ContentPublishingPlanFetcher(final ContentService contentService) {
        _contentService = contentService;
    }

    @DgsQuery(field = "contentPublishingPlans")
    public List<ContentPublishingPlan> contentPublishingPlans() {
        return _contentService.getAllPublishingPlans();
    }
}
