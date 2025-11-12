package dev.hireben.url_shortener.url.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.hireben.url_shortener.url.entity.UrlMapping;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

}
