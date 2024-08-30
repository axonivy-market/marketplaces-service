package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMappingConstants {
  public static final String ROOT = "/";
  public static final String API = ROOT + "api";
  public static final String SYNC = "sync";
  public static final String PRODUCT = API + "/product";
  public static final String PRODUCT_DETAILS = API + "/product-details";
  public static final String FEEDBACK = API + "/feedback";
  public static final String SWAGGER_URL = "/swagger-ui/index.html";
  public static final String GIT_HUB_LOGIN = "/github/login";
  public static final String AUTH = "/auth";
  public static final String BY_ID = "/{id}";
  public static final String BY_ID_AND_VERSION = "/{id}/{version}";
  public static final String BEST_MATCH_BY_ID_AND_VERSION = "/{id}/{version}/bestmatch";
  public static final String VERSIONS_BY_ID = "/{id}/versions";
  public static final String PRODUCT_BY_ID = "/product/{id}";
  public static final String PRODUCT_RATING_BY_ID = "/product/{id}/rating";
  public static final String INSTALLATION_COUNT_BY_ID = "/installationcount/{id}";
  public static final String PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION = "/productjsoncontent/{productId}/{version}";
  public static final String VERSIONS_IN_DESIGNER = "/{id}/designerversions";
  public static final String DESIGNER_INSTALLATION_BY_PRODUCT_ID_AND_DESIGNER_VERSION = "/installation/{productId}/designer/{designerVersion}";
  public static final String DESIGNER_INSTALLATION_BY_PRODUCT_ID = "/installation/{productId}/designer";
  public static final String CUSTOM_SORT = "custom-sort";
}