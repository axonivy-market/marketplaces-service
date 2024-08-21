package com.axonivy.market.entity.productjsonfilecontent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Dependency {
  private String groupId;
  private String artifactId;
  private String version;
  private String type;
}
