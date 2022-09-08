package com.worldfamilyenglish.vegas.persistence;


import com.worldfamilyenglish.vegas.domain.ContentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContentInfoRepository extends JpaRepository<ContentInfo, Long> {

}
