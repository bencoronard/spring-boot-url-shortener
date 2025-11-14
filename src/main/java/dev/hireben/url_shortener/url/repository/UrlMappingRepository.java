package dev.hireben.url_shortener.url.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.hireben.url_shortener.url.entity.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, String> {

  Slice<UrlMapping> findAllByCreatedById(Pageable pageable, Long createdById);

  UrlMapping findByShortUrlPathAndCreatedById(String shortUrlPath, Long createdById);

  UrlMapping findByOriginalUrlAndCreatedById(String originalUrl, Long createdById);

  void deleteByShortUrlPathAndCreatedById(String shortUrlPath, Long createdById);

}
