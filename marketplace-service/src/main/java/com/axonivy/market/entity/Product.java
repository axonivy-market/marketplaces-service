package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.PRODUCT;
import com.axonivy.market.github.model.MavenArtifact;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(PRODUCT)
public class Product implements Serializable {
  @Serial
  private static final long serialVersionUID = -8770801877877277258L;
  @Id
  private String id;
  private String marketDirectory;
  @JsonProperty
  private Map<String, String> names;
  private String version;
  @JsonProperty
  private Map<String, String> shortDescriptions;
  private String logoUrl;
  private Boolean listed;
  private String type;
  private List<String> tags;
  private String vendor;
  private String vendorUrl;
  private String platformReview;
  private String cost;
  private String repositoryName;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private String compatibility;
  private Boolean validate;
  private Boolean contactUs;
  private int installationCount;
  private Date newestPublishedDate;
  private String newestReleaseVersion;
  @Transient
  private ProductModuleContent productModuleContent;
  private List<MavenArtifact> artifacts;
  private Boolean synchronizedInstallationCount;
  private Integer customOrder;
  private List<String> releasedVersions;
  @Transient
  private String metaProductJsonUrl;
  private String logoId;
  @LastModifiedDate
  private Date updatedAt;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(id, ((Product) obj).getId()).isEquals();
  }

}
