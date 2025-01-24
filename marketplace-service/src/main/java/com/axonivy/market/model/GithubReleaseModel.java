package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubReleaseModel extends RepresentationModel<GithubReleaseModel> {
  @Schema(description = "Version of release", example = "12.0.3")
  private String name;

  @Schema(description = "Body of release", example = "- [IVYPORTAL-18158](https://1ivy.atlassian.net/browse/IVYPORTAL-18158) Implement File Preview to Portal Components @nhthinh-axonivy (#1443)")
  private String body;

  @Schema(description = "Published date of release", example = "2025-01-20")
  private LocalDate publishedAt;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(name).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(name, ((GithubReleaseModel) obj).getName()).isEquals();
  }
}
