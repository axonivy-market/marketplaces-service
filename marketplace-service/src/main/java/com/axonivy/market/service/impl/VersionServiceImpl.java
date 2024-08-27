package com.axonivy.market.service.impl;

import com.axonivy.market.comparator.ArchivedArtifactsComparator;
import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.VersionUtils;
import com.axonivy.market.util.XmlReaderUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
@Getter
public class VersionServiceImpl implements VersionService {

  private final GHAxonIvyProductRepoService gitHubService;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
  private final ProductRepository productRepository;
  private final ProductJsonContentRepository productJsonContentRepository;
  @Getter
  private String repoName;
  private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
  private List<MavenArtifact> artifactsFromMeta;
  private MavenArtifactVersion proceedDataCache;
  private MavenArtifact metaProductArtifact;
  private final LatestVersionComparator latestVersionComparator = new LatestVersionComparator();
  @Getter
  private String productJsonFilePath;
  private String productId;
  private final ObjectMapper mapper = new ObjectMapper();

  public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService,
      MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepository,
      ProductJsonContentRepository productJsonContentRepository) {
    this.gitHubService = gitHubService;
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
    this.productRepository = productRepository;

    this.productJsonContentRepository = productJsonContentRepository;
  }

  private void resetData() {
    repoName = null;
    archivedArtifactsMap = new HashMap<>();
    artifactsFromMeta = Collections.emptyList();
    proceedDataCache = null;
    metaProductArtifact = null;
    productJsonFilePath = null;
    productId = null;
  }

  public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion,
      String designerVersion) {
    List<MavenArtifactVersionModel> results = new ArrayList<>();
    resetData();

    this.productId = productId;
    artifactsFromMeta = getProductMetaArtifacts(productId);
    List<String> versionsToDisplay = VersionUtils.getVersionsToDisplay(getVersionsFromMavenArtifacts(), isShowDevVersion, designerVersion);
    proceedDataCache = mavenArtifactVersionRepository.findById(productId).orElse(new MavenArtifactVersion(productId));
    metaProductArtifact = artifactsFromMeta.stream()
        .filter(artifact -> artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findAny()
        .orElse(new MavenArtifact());

    sanitizeMetaArtifactBeforeHandle();

    boolean isNewVersionDetected = handleArtifactForVersionToDisplay(versionsToDisplay, results);
    if (isNewVersionDetected) {
      mavenArtifactVersionRepository.save(proceedDataCache);
    }
    return results;
  }

  @Override
  public Map<String, Object> getProductJsonContentFromNameAndVersion(String productId, String version)
      throws JsonProcessingException {
    ProductJsonContent productJsonContent = productJsonContentRepository.findByProductIdAndVersion(productId, version);
    if (ObjectUtils.isEmpty(productJsonContent)) {
      return new HashMap<>();
    }
    Map<String, Object> result = mapper.readValue(productJsonContent.getContent(), Map.class);
    result.computeIfAbsent("name", k -> productJsonContent.getName());
    return result;
  }

  @Override
  public List<String> getVersionsForDesigner(String productId, Boolean isShowDevVersion, String designerVersion) {
    Product product = productRepository.findById(productId).orElse(null);
    if (ObjectUtils.isEmpty(product) || ObjectUtils.isEmpty(product.getProductModuleContents())) {
      return new ArrayList<>();
    }
    List<String> versionList = product.getProductModuleContents().stream().map(ProductModuleContent::getTag)
        .map(VersionUtils::convertTagToVersion).toList();
    return VersionUtils.getVersionsToDisplay(versionList, isShowDevVersion, designerVersion);
  }

  public boolean handleArtifactForVersionToDisplay(List<String> versionsToDisplay,
      List<MavenArtifactVersionModel> result) {
    boolean isNewVersionDetected = false;
    for (String version : versionsToDisplay) {
      List<MavenArtifactModel> artifactsInVersion = convertMavenArtifactsToModels(artifactsFromMeta, version);
      List<MavenArtifactModel> productArtifactModels = proceedDataCache.getProductArtifactWithVersionReleased()
          .get(version);
      if (productArtifactModels == null) {
        isNewVersionDetected = true;
        productArtifactModels = updateArtifactsInVersionWithProductArtifact(version);
      }
      artifactsInVersion.addAll(productArtifactModels);
      result.add(new MavenArtifactVersionModel(version, artifactsInVersion.stream().distinct().toList()));
    }
    return isNewVersionDetected;
  }

  public List<MavenArtifactModel> updateArtifactsInVersionWithProductArtifact(String version) {
    List<MavenArtifactModel> productArtifactModels = convertMavenArtifactsToModels(getProductJsonByVersion(version),
        version);
    proceedDataCache.getVersions().add(version);
    proceedDataCache.getProductArtifactWithVersionReleased().put(version, productArtifactModels);
    return productArtifactModels;
  }

  public List<MavenArtifact> getProductMetaArtifacts(String productId) {
    Product productInfo = productRepository.findById(productId).orElse(new Product());
    String fullRepoName = productInfo.getRepositoryName();
    if (StringUtils.isNotEmpty(fullRepoName)) {
      repoName = getRepoNameFromMarketRepo(fullRepoName);
    }
    return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
  }

  public void sanitizeMetaArtifactBeforeHandle() {
    artifactsFromMeta.remove(metaProductArtifact);
    artifactsFromMeta.forEach(artifact -> {
      List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(
          Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).stream()
              .sorted(new ArchivedArtifactsComparator()).toList());
      Collections.reverse(archivedArtifacts);
      archivedArtifactsMap.put(artifact.getArtifactId(), archivedArtifacts);
    });
  }

  public List<String> getVersionsFromMavenArtifacts() {
    Set<String> versions = new HashSet<>();
    for (MavenArtifact artifact : artifactsFromMeta) {
      versions.addAll(
          getVersionsFromArtifactDetails(artifact.getRepoUrl(), artifact.getGroupId(), artifact.getArtifactId()));
      Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).forEach(
          archivedArtifact -> versions.addAll(
              getVersionsFromArtifactDetails(artifact.getRepoUrl(), archivedArtifact.getGroupId(),
                  archivedArtifact.getArtifactId())));
    }
    List<String> versionList = new ArrayList<>(versions);
    versionList.sort(new LatestVersionComparator());
    return versionList;
  }

  @Override
  public List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactID) {
    List<String> versions = new ArrayList<>();
    String baseUrl = buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactID);
    if (StringUtils.isNotBlank(baseUrl)) {
      versions.addAll(XmlReaderUtils.readXMLFromUrl(baseUrl));
    }
    return versions;
  }

  @Override
  public String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
    if (StringUtils.isAnyBlank(groupId, artifactID)) {
      return StringUtils.EMPTY;
    }
    repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    groupId = groupId.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.format(MavenConstants.METADATA_URL_FORMAT, repoUrl, groupId, artifactID);
  }

  public List<MavenArtifact> getProductJsonByVersion(String version) {
    List<MavenArtifact> result = new ArrayList<>();
    String versionTag = getVersionTag(version);
    productJsonFilePath = buildProductJsonFilePath();
    try {
      GHContent productJsonContent = gitHubService.getContentFromGHRepoAndTag(repoName, productJsonFilePath,
          versionTag);
      if (Objects.isNull(productJsonContent)) {
        return result;
      }
      result = gitHubService.convertProductJsonToMavenProductInfo(productJsonContent);
    } catch (IOException e) {
      log.warn("Can not get the product.json from repo {} by path in {} version {}", repoName, productJsonFilePath,
          versionTag);
    }
    return result;
  }

  public String getVersionTag(String version) {
    String versionTag = "v" + version;
    if (NonStandardProduct.PORTAL.getId().equals(productId)) {
      versionTag = version;
    }
    return versionTag;
  }

  public String buildProductJsonFilePath() {
    String pathToProductFolderFromTagContent = metaProductArtifact.getArtifactId();
    GitHubUtils.getNonStandardProductFilePath(productId);
    productJsonFilePath = String.format(GitHubConstants.PRODUCT_JSON_FILE_PATH_FORMAT,
        pathToProductFolderFromTagContent);
    return productJsonFilePath;
  }

  public MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
    String artifactName = artifact.getName();
    if (StringUtils.isBlank(artifactName)) {
      artifactName = GitHubUtils.convertArtifactIdToName(artifact.getArtifactId());
    }
    artifact.setType(Optional.ofNullable(artifact.getType()).orElse("iar"));
    artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
    return new MavenArtifactModel(artifactName, buildDownloadUrlFromArtifactAndVersion(artifact, version),
        artifact.getIsProductArtifact());
  }

  public List<MavenArtifactModel> convertMavenArtifactsToModels(List<MavenArtifact> artifacts, String version) {
    List<MavenArtifactModel> results = new ArrayList<>();
    if (!CollectionUtils.isEmpty(artifacts)) {
      for (MavenArtifact artifact : artifacts) {
        MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version);
        results.add(mavenArtifactModel);
      }
    }
    return results;
  }

  public String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version) {
    String groupIdByVersion = artifact.getGroupId();
    String artifactIdByVersion = artifact.getArtifactId();
    String repoUrl = Optional.ofNullable(artifact.getRepoUrl()).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(
        artifact.getArtifactId(), version);

    if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
      groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
      artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
    }
    groupIdByVersion = groupIdByVersion.replace(CommonConstants.DOT_SEPARATOR, CommonConstants.SLASH);
    return String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, repoUrl, groupIdByVersion, artifactIdByVersion,
        version, artifactIdByVersion, version, artifact.getType());
  }

  public ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String artifactId, String version) {
    List<ArchivedArtifact> archivedArtifacts = archivedArtifactsMap.get(artifactId);

    if (CollectionUtils.isEmpty(archivedArtifacts)) {
      return null;
    }
    for (ArchivedArtifact archivedArtifact : archivedArtifacts) {
      if (latestVersionComparator.compare(archivedArtifact.getLastVersion(), version) <= 0) {
        return archivedArtifact;
      }
    }
    return null;
  }

  public String getRepoNameFromMarketRepo(String fullRepoName) {
    String[] repoNamePart = fullRepoName.split("/");
    return repoNamePart[repoNamePart.length - 1];
  }
}
