package dev.hireben.url_shortener.url.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.hireben.url_shortener.url.entity.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, String> {

  Slice<String> findAllShortUrlPathByCreatedById(Pageable pageable, Long createdById);

  String findOriginalUrlByShortUrlPathAndCreatedById(String shortUrlPath, Long createdById);

  String findShortUrlPathByOriginalUrlAndCreatedById(String originalUrl, Long createdById);

  void deleteByShortUrlPathAndCreatedById(String shortUrlPath, Long createdById);

}
