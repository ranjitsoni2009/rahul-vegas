package com.worldfamilyenglish.vegas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.worldfamilyenglish.vegas.domain.Child;
import com.worldfamilyenglish.vegas.domain.Parent;
import com.worldfamilyenglish.vegas.persistence.IChildRepository;
import com.worldfamilyenglish.vegas.persistence.IParentRepository;
import com.worldfamilyenglish.vegas.types.CreateChildInput;
import com.worldfamilyenglish.vegas.types.CreateParentInput;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParentService {

    private final IParentRepository _parentRepository;
    private final IChildRepository _childRepository;

    @Autowired
    public ParentService(final IParentRepository parentRepository, final IChildRepository childRepository) {
        _parentRepository = parentRepository;
        _childRepository = childRepository;
    }

    @Transactional
    public Parent createParent(final CreateParentInput createParentInput) {

        Parent parentDomainObject = Parent.builder()
                .displayName(createParentInput.getDisplayName())
                .externalId(createParentInput.getUniqueExternalId())
                .build();

        parentDomainObject = _parentRepository.save(parentDomainObject);

        List<CreateChildInput> childrenInputs = createParentInput.getChildren();
        List<Child> childDomainObjects = createChildrenDomainObjects(parentDomainObject, childrenInputs);

        childDomainObjects = _childRepository.saveAll(childDomainObjects);

        LOG.info("Created children {}", childDomainObjects);

        parentDomainObject.setChildren(childDomainObjects);

        LOG.info("Created new parent with external id '{}'", parentDomainObject.getExternalId());

        return parentDomainObject;
    }

    private List<Child> createChildrenDomainObjects(final Parent parentDomainObject, final List<CreateChildInput> childrenInputs) {
        return childrenInputs.stream().map(childInput -> Child.builder()
                .externalId(childInput.getUniqueExternalId())
                .displayName(childInput.getDisplayName())
                .capLevel(childInput.getCapLevel())
                .parent(parentDomainObject)
                .build())
                .toList();
    }

    @Transactional
    public List<Child> getAssociatedChildren(final String externalChildId, final CreateParentInput createParentInput) {

    	List<Child> children = new ArrayList<>();
    	
    	if (Objects.isNull(createParentInput) && !StringUtils.hasText(externalChildId)) {
    		throw new IllegalArgumentException("Neither existing externalChildId found nor createParentInput found for creating Parent and Child");
    	}
    	
    	if (Objects.nonNull(createParentInput) && StringUtils.hasText(externalChildId)) {
    		Parent createdParent = createOrUpdateParent(createParentInput);
    		if (Objects.nonNull(createdParent)) {
    			children = createdParent.getChildren();
    		}
    		else {
    			throw new IllegalArgumentException("Neither Parent created nor updated with uniqueExternalId %s".formatted(createParentInput.getUniqueExternalId()));
    		}
    	}

    	if (CollectionUtils.isEmpty(children)) {
    		Optional<Child> foundChildDomainObject = _childRepository.findByExternalId(externalChildId);
    		if (foundChildDomainObject.isEmpty()) {
    			throw new IllegalArgumentException("Could not find child with externalChildId '%s', did you already create that record?".formatted(externalChildId));
    		}

    		children.add(foundChildDomainObject.get());
    	}

    	return children;
    }

	@Transactional
    public Parent createOrUpdateParent(final CreateParentInput createParentInput) {

    	Parent parentDomainObject = null;

    	Optional<Parent> foundParent = getParent(createParentInput.getUniqueExternalId());

    	if (foundParent.isPresent()) {

    		parentDomainObject = foundParent.get();

    		parentDomainObject.setExternalId(createParentInput.getUniqueExternalId());
    		parentDomainObject.setDisplayName(createParentInput.getDisplayName());

    		parentDomainObject = _parentRepository.save(parentDomainObject);

    		List<Child> childDomainObjects = updateChildrenDomainObjects(parentDomainObject, createParentInput.getChildren());

    		childDomainObjects = _childRepository.saveAll(childDomainObjects);

    		LOG.info("Updated children {}", childDomainObjects);

    		parentDomainObject.setChildren(childDomainObjects);

    		LOG.info("Updated parent with external id '{}'", parentDomainObject.getExternalId());
    	} else {
    		parentDomainObject = createParent(createParentInput);
    	}

    	return parentDomainObject;

    }
  
    private List<Child> updateChildrenDomainObjects(final Parent parentDomainObject, final List<CreateChildInput> createChildInputs) {
    	
    	List<Child> existingChildren = parentDomainObject.getChildren();
    	List<CreateChildInput> clonedCreateChildInput = createChildInputs.stream().collect(Collectors.toList());
    			
    	if(!parentDomainObject.getChildren().isEmpty()) {
    		for (Child child : existingChildren) {
    			for (CreateChildInput createChildInput : createChildInputs) {
    				
    				if (child.getExternalId().equals(createChildInput.getUniqueExternalId())) {
    					mapCreateChildInputToChildDomain(child, createChildInput, parentDomainObject);
    					clonedCreateChildInput.remove(createChildInput);
    				}
    			}
    		}
    	}
    	
    	if(!clonedCreateChildInput.isEmpty()) {
    		
    		Child newChild = null;
    		for (CreateChildInput createChildInput : clonedCreateChildInput) {
    			newChild = new Child();
    			mapCreateChildInputToChildDomain(newChild, createChildInput, parentDomainObject);
				existingChildren.add(newChild);
    		}
    	}
    	
    	return existingChildren;
	}

	private void mapCreateChildInputToChildDomain(Child child, CreateChildInput createChildInput, Parent parentDomainObject) {
		child.setCapLevel(createChildInput.getCapLevel());
		child.setExternalId(createChildInput.getUniqueExternalId());
		child.setDisplayName(createChildInput.getDisplayName());
		child.setParent(parentDomainObject);		
	}

	public Optional<Parent> getParent(final long id) {
        return _parentRepository.findById(id);
    }

    public Optional<Parent> getParent(final String externalId) {
        return _parentRepository.findByExternalId(externalId);
    }
}
