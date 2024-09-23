package com.axonivy.market.maven.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metadata {
  @Id
  private String metadataUrl;
  private LocalDateTime lastUpdated;
  @Transient
  private String artifactId;
  @Transient
  private String groupId;
  @Transient
  private String latest;
  @Transient
  private String release;
  @Transient
  private List<String> versions;
  private String repoUrl;
  private String type;
  private String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Metadata that = (Metadata) o;
    return Objects.equals(metadataUrl, that.metadataUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(metadataUrl);
  }
}
