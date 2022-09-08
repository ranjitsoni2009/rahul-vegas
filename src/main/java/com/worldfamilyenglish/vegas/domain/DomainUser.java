package com.worldfamilyenglish.vegas.domain;

import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

@Accessors(prefix = "")
@Builder
@Getter
@Setter
@ToString
@Entity
@Table(name = "domain_user")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DomainUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "family_name")
    private String familyName;

    @Column(name = "given_name")
    private String givenName;

    @EqualsAndHashCode.Include
    @Column(name = "active_directory_id")
    private String activeDirectoryId;

    @EqualsAndHashCode.Include
    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Availability> availabilities = new ArrayList<>();

    @Column(name = "can_teach")
    private boolean canTeach;

    @Column(name = "can_admin")
    private boolean canAdmin;


//    private boolean _canLiveOps;
//    private boolean _canTrafficManage;
//    private boolean _canSupport;
//    private boolean _canQA;

    @OneToMany(mappedBy = "domainUser", orphanRemoval = true)
    private Set<ContentPerformingAccreditation> contentPerformingAccreditations = new LinkedHashSet<>();


    @CreationTimestamp
    @Column(name = "created")
    private OffsetDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private OffsetDateTime updated;


}
