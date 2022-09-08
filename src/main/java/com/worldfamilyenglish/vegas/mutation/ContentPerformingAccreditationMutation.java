package com.worldfamilyenglish.vegas.mutation;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.ContentPerformingAccreditation;
import com.worldfamilyenglish.vegas.service.ContentService;
import com.worldfamilyenglish.vegas.types.CreateContentPerformingAccreditationInput;

@DgsComponent
public class ContentPerformingAccreditationMutation {

    private ContentService _contentService;

    public ContentPerformingAccreditationMutation(final ContentService contentService) {
        _contentService = contentService;
    }

    @DgsMutation
    ContentPerformingAccreditation createContentPerformingAccreditation(@InputArgument CreateContentPerformingAccreditationInput input) {
        ContentPerformingAccreditation accreditation = _contentService.createContentPerformingAccreditation(input.getDomainUserId(), input.getContentInfoId());
        return accreditation;
    }
}
