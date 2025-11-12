package dev.hireben.url_shortener.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.hireben.url_shortener.auth.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

}
