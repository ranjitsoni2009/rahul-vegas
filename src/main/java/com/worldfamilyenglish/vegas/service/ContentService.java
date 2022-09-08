package com.worldfamilyenglish.vegas.service;

import com.neovisionaries.i18n.CountryCode;
import com.worldfamilyenglish.vegas.domain.*;
import com.worldfamilyenglish.vegas.persistence.IContentInfoRepository;
import com.worldfamilyenglish.vegas.persistence.IContentPerformingAccreditationRepository;
import com.worldfamilyenglish.vegas.persistence.IContentPublishingPlanRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {

    private final IDomainUserService _domainUserService;
    private final IContentInfoRepository _contentInfoRepository;
    private final IContentPublishingPlanRepository _contentPublishingPlanRepository;

    private final IContentPerformingAccreditationRepository _contentPerformingAccreditationRepository;

    @Autowired
    public ContentService(final IDomainUserService domainUserService, final IContentInfoRepository contentInfoRepository, final IContentPublishingPlanRepository contentPublishingPlanRepository, final IContentPerformingAccreditationRepository contentPerformingAccreditationRepository) {
        _domainUserService = domainUserService;
        _contentInfoRepository = contentInfoRepository;
        _contentPublishingPlanRepository = contentPublishingPlanRepository;
        _contentPerformingAccreditationRepository = contentPerformingAccreditationRepository;
    }

    public List<ContentInfo> getAllContent() {
        return _contentInfoRepository.findAll();
    }

    public ContentInfo createContentInfo(final String name, final String description, final CapLevel level, final String externalUrl) {
        ContentInfo contentInfo = ContentInfo
                .builder()
                .name(name)
                .description(description)
                .level(level)
                .externalUrl(externalUrl)
                .build();

        return _contentInfoRepository.save(contentInfo);
    }

    public List<ContentPublishingPlan> getAllPublishingPlans() {
        return _contentPublishingPlanRepository.findAll();
    }

    public ContentPublishingPlan createPublishingPlan(final long contentId, final OffsetDateTime from, final OffsetDateTime to, final List<CountryCode> countries) {
        ContentInfo foundContent = getContentInfo(contentId);

        ContentPublishingPlan publishingPlan = ContentPublishingPlan
                .builder()
                .content(foundContent)
                .from(from)
                .to(to)
                .countries(countries)
                .build();

        return _contentPublishingPlanRepository.save(publishingPlan);
    }

    public ContentPerformingAccreditation createContentPerformingAccreditation(final long domainUserId, final long contentInfoId) {
        DomainUser domainUser = getUser(domainUserId);
        ContentInfo contentInfo = getContentInfo(contentInfoId);

        ContentPerformingAccreditation accreditation = ContentPerformingAccreditation
                .builder()
                .contentInfo(contentInfo)
                .domainUser(domainUser)
                .isAccredited(true)
                .build();

        return _contentPerformingAccreditationRepository.save(accreditation);
    }

    public List<ContentPerformingAccreditation> getAllPerformingAccreditations() {
        return _contentPerformingAccreditationRepository.findAll();
    }

    private DomainUser getUser(final long domainUserId) {
        Optional<DomainUser> foundDomainUser = _domainUserService.getUser(domainUserId);
        if (foundDomainUser.isEmpty()) {
            throw new IllegalArgumentException("Domain user with id '%d' could not be found.".formatted(domainUserId));
        }

        return foundDomainUser.get();
    }

    @NotNull
    public ContentInfo getContentInfo(final long contentId) {
        Optional<ContentInfo> foundContent = _contentInfoRepository.findById(contentId);

        if (foundContent.isEmpty()) {
            throw new IllegalArgumentException("Could not find any content with id '%d', is that a valid content id?".formatted(contentId));
        }

        return foundContent.get();
    }

    public boolean isContentPublishedDuringPeriod(final OffsetDateTime timeToCheck, final ContentInfo contentInfo) {
        return _contentPublishingPlanRepository.findByFromLessThanEqualAndToGreaterThanEqualAndContent_Id(timeToCheck, timeToCheck, contentInfo.getId()).size() > 0;
    }

    public List<ContentPublishingPlan> getAllPublishingPlans(final OffsetDateTime from, final OffsetDateTime to) {
        return _contentPublishingPlanRepository.findByToGreaterThanEqualAndFromLessThanEqual(from, to);
    }
}
