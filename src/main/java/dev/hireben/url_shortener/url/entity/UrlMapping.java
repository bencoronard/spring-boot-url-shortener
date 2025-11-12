package dev.hireben.url_shortener.url.entity;

import java.time.Instant;

import dev.hireben.url_shortener.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "t_url_mappings", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "original_url",
    "created_by" }))
public class UrlMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, updatable = false, length = 255)
  private String originalUrl;

  @Column(nullable = false, unique = true, updatable = false, length = 255)
  private String shortUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", nullable = false, updatable = false)
  private User createdBy;

  @Column(nullable = false, updatable = false, length = 255)
  private Instant createdAt;

}
