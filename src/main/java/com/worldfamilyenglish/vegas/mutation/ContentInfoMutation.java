package com.worldfamilyenglish.vegas.mutation;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.ContentInfo;
import com.worldfamilyenglish.vegas.service.ContentService;
import com.worldfamilyenglish.vegas.types.CreateContentInfoInput;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class ContentInfoMutation {

    private final ContentService _contentService;

    @Autowired
    public ContentInfoMutation(final ContentService contentService) {
        _contentService = contentService;
    }

    @DgsMutation
    public ContentInfo createContentInfo(@InputArgument CreateContentInfoInput input) {
        ContentInfo createdContentInfo = _contentService.createContentInfo(
            input.getName(), input.getDescription(), input.getLevel(), input.getExternalUrl());
        return createdContentInfo;
    }
}
