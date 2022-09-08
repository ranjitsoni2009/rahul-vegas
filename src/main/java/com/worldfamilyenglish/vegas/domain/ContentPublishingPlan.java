package com.worldfamilyenglish.vegas.domain;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.neovisionaries.i18n.CountryCode;
import com.worldfamilyenglish.vegas.persistence.CountryCodeAttributeConvertor;

import lombok.experimental.Accessors;

@Accessors(prefix = "")
@Builder
@Getter
@Setter
@Entity
@Table(name = "content_publishing_plan")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentPublishingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_time")
    @EqualsAndHashCode.Include
    private OffsetDateTime from;

    @Column(name = "to_time")
    @EqualsAndHashCode.Include
    private OffsetDateTime to;

    @NotNull
    @ManyToOne
    @EqualsAndHashCode.Include
    private ContentInfo content;

    @Convert(converter = CountryCodeAttributeConvertor.class)
    @Column(name = "countries")
    private List<CountryCode> countries;

    @CreationTimestamp
    @Column(name = "created", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updated;

}
