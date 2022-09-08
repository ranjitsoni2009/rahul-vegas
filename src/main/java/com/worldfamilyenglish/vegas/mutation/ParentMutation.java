package com.worldfamilyenglish.vegas.mutation;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.service.ParentService;
import com.worldfamilyenglish.vegas.types.CreateParentInput;

@DgsComponent
public class ParentMutation {

    private final ParentService _parentService;

    public ParentMutation(final ParentService parentService) {
        _parentService = parentService;
    }

    @DgsMutation(field = "createParent")
    public Parent createParent(@InputArgument("input") CreateParentInput createParentInput) {
        Parent parent = _parentService.createParent(createParentInput);
        return parent;
    }
}
