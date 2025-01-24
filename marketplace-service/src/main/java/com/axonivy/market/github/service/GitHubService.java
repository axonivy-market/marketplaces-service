package com.axonivy.market.github.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.model.GithubReleaseModel;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

public interface GitHubService {

  GitHub getGitHub() throws IOException;

  GitHub getGitHub(String accessToken) throws IOException;

  GHOrganization getOrganization(String orgName) throws IOException;

  GHRepository getRepository(String repositoryPath) throws IOException;

  List<GHTag> getRepositoryTags(String repositoryPath) throws IOException;

  List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GitHubAccessTokenResponse getAccessToken(String code, GitHubProperty gitHubProperty)
      throws Oauth2ExchangeCodeException, MissingHeaderException;

  User getAndUpdateUser(String accessToken);

  void validateUserInOrganizationAndTeam(String accessToken, String team, String org) throws UnauthorizedException;

  List<ProductSecurityInfo> getSecurityDetailsForAllProducts(String accessToken, String orgName);

  List<GithubReleaseModel> getReleases(Product product) throws IOException;
}
