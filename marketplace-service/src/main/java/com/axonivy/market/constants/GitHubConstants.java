package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GitHubConstants {
  public static final String AXONIVY_MARKET_ORGANIZATION_NAME = "axonivy-market";
  public static final String AXONIVY_MARKETPLACE_REPO_NAME = "market";
  public static final String AXONIVY_MARKETPLACE_PATH = "market";
  public static final String GITHUB_PROVIDER_NAME = "GitHub";
  public static final String GITHUB_GET_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
  public static final String README_FILE_LOCALE_REGEX = "_(..)";
  public static final String STANDARD_TAG_PREFIX = "v";
  public static final String COMMON_IMAGES_FOLDER_NAME = "images";
  public static final String POM_FILE = "pom.xml";
  public static final String VARIABLES_FILE_DIR = "variables.yaml.file";
  public static final String VARIABLES_PARENT_PATH = "../";


  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Json {
    public static final String TOKEN = "token";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String CODE = "code";
    public static final String USER_ID = "id";
    public static final String USER_NAME = "name";
    public static final String USER_AVATAR_URL = "avatar_url";
    public static final String USER_LOGIN_NAME = "login";
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Url {
    private static final String BASE_URL = "https://api.github.com";
    public static final String USER = BASE_URL + "/user";
    public static final String USER_ORGS = USER + "/orgs";
  }
}
