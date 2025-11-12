package dev.hireben.url_shortener.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.hireben.url_shortener.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmail(String email);

  User findByEmail(String email);

}
