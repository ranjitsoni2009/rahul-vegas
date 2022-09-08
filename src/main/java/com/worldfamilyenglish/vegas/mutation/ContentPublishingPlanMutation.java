package com.worldfamilyenglish.vegas.mutation;

import com.neovisionaries.i18n.CountryCode;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.ContentPublishingPlan;
import com.worldfamilyenglish.vegas.service.ContentService;
import com.worldfamilyenglish.vegas.types.CreateContentPublishingPlanInput;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DgsComponent
public class ContentPublishingPlanMutation {

    private final ContentService _contentService;

    @Autowired
    public ContentPublishingPlanMutation(final ContentService contentService) {
        _contentService = contentService;
    }

    @DgsMutation
    public ContentPublishingPlan createContentPublishingPlan(@InputArgument final CreateContentPublishingPlanInput input) {

        List<String> countryStrings = input.getCountries();
        List<CountryCode> countryObjects = null;

        if (countryStrings != null) {
            countryObjects = countryStrings
                    .stream()
                    .map(CountryCode::getByAlpha2Code)
                    .toList();
        }

        return _contentService.createPublishingPlan(input.getContentId(), input.getFrom(), input.getTo(), countryObjects);
    }
}
