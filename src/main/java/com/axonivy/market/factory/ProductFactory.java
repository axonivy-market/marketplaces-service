package com.axonivy.market.factory;

import static com.axonivy.market.constants.CommonConstants.LOGO_FILE;
import static com.axonivy.market.constants.CommonConstants.META_FILE;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.util.GithubUtils;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductFactory {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static Product mappingByGHContent(Product product, GHContent content) {
    var contentName = content.getName();
    if (contentName.endsWith(META_FILE)) {
      mappingByMetaJSONFile(product, content);
    }
    if (contentName.endsWith(LOGO_FILE)) {
      product.setLogoUrl(GithubUtils.getDownloadUrl(content));
    }
    return product;
  }

  public static Product mappingByMetaJSONFile(Product product, GHContent ghContent) {
    Meta meta = null;
    try {
      meta = jsonDecode(ghContent);
    } catch (Exception e) {
      log.error("Mapping from Meta file by GHContent failed", e);
      return product;
    }

    product.setId(meta.getId());
    product.setName(meta.getName());
    product.setMarketDirectory(extractParentDirectory(ghContent));
    product.setListed(meta.getListed());
    product.setType(meta.getType());
    product.setTags(meta.getTags());
    product.setVersion(meta.getVersion());
    product.setShortDescription(meta.getDescription());
    product.setVendor(meta.getVendor());
    product.setVendorImage(meta.getVendorImage());
    product.setVendorUrl(meta.getVendorUrl());
    product.setPlatformReview(meta.getPlatformReview());
    product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
    product.setLanguage(meta.getLanguage());
    product.setIndustry(meta.getIndustry());
    extractSourceUrl(product, meta);
    updateLatestReleaseDateForProduct(product);
    return product;
  }

  private static void updateLatestReleaseDateForProduct(Product product) {
    if (StringUtils.isBlank(product.getRepositoryName())) {
      return;
    }
    try {
      GHRepository productRepo = GithubUtils.getGHRepoByPath(product.getRepositoryName());
      GHTag lastTag = CollectionUtils.firstElement(productRepo.listTags().toList());
      product.setNewestPublishDate(lastTag.getCommit().getCommitDate());
      product.setNewestReleaseVersion(lastTag.getName());
    } catch (Exception e) {
      log.error("Cannot find repository by path {} {}", product.getRepositoryName(), e);
    }
  }

  private static String extractParentDirectory(GHContent ghContent) {
    return ghContent.getPath().replace(ghContent.getName(), EMPTY);
  }

  private static void extractSourceUrl(Product product, Meta meta) {
    var sourceUrl = meta.getSourceUrl();
    if (StringUtils.isBlank(sourceUrl)) {
      return;
    }
    String[] tokens = sourceUrl.split(SLASH);
    var tokensLength = tokens.length;
    var repositoryPath = sourceUrl;
    if (tokensLength > 1) {
      repositoryPath = String.join(SLASH, tokens[tokensLength - 2], tokens[tokensLength - 1]);
    }
    product.setRepositoryName(repositoryPath);
    product.setSourceUrl(sourceUrl);
  }

  private static Meta jsonDecode(GHContent ghContent) throws StreamReadException, DatabindException, IOException {
    return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
  }
}
