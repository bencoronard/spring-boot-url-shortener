package dev.hireben.url_shortener.auth.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import dev.hireben.url_shortener.url.entity.UrlMapping;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "m_users", schema = "public")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false, length = 255)
  private String email;

  @Column(nullable = false, updatable = false, length = 255)
  private String password;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Builder.Default
  @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UrlMapping> urlMappings = new HashSet<>();

}
