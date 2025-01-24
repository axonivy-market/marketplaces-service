package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.MetaConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.GitHubRepoMeta;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.*;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.service.MetadataService;
import com.axonivy.market.service.ProductContentService;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.MetadataReaderUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.axonivy.market.constants.CommonConstants.DOT_SEPARATOR;
import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.CommonConstants.PLUS;
import static com.axonivy.market.constants.CommonConstants.COMPATIBILITY_RANGE_FORMAT;
import static com.axonivy.market.constants.MavenConstants.*;
import static com.axonivy.market.constants.ProductJsonConstants.EN_LANGUAGE;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;
import static com.axonivy.market.enums.DocumentField.MARKET_DIRECTORY;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;
import static com.axonivy.market.enums.FileStatus.ADDED;
import static com.axonivy.market.enums.FileStatus.MODIFIED;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {
  private static final String INITIAL_VERSION = "1.0";
  private final ProductRepository productRepo;
  private final ProductModuleContentRepository productModuleContentRepo;
  private final GHAxonIvyMarketRepoService axonIvyMarketRepoService;
  private final GHAxonIvyProductRepoService axonIvyProductRepoService;
  private final GitHubRepoMetaRepository gitHubRepoMetaRepo;
  private final GitHubService gitHubService;
  private final ProductCustomSortRepository productCustomSortRepo;
  private final MavenArtifactVersionRepository mavenArtifactVersionRepo;
  private final MetadataSyncRepository metadataSyncRepo;
  private final MetadataRepository metadataRepo;
  private final ProductJsonContentRepository productJsonContentRepo;
  private final ImageRepository imageRepo;
  private final ImageService imageService;
  private final ProductContentService productContentService;
  private final ExternalDocumentService externalDocumentService;
  private final MetadataService metadataService;
  private final ProductMarketplaceDataService productMarketplaceDataService;
  private final ProductMarketplaceDataRepository productMarketplaceDataRepo;
  private GHCommit lastGHCommit;
  private final VersionService versionService;
  private GitHubRepoMeta marketRepoMeta;
  @Value("${market.github.market.branch}")
  private String marketRepoBranch;

  public ProductServiceImpl(ProductRepository productRepo, ProductModuleContentRepository productModuleContentRepo,
      GHAxonIvyMarketRepoService axonIvyMarketRepoService, GHAxonIvyProductRepoService axonIvyProductRepoService,
      GitHubRepoMetaRepository gitHubRepoMetaRepo, GitHubService gitHubService,
      ProductCustomSortRepository productCustomSortRepo, MavenArtifactVersionRepository mavenArtifactVersionRepo,
      ProductJsonContentRepository productJsonContentRepo, ImageRepository imageRepo,
      MetadataSyncRepository metadataSyncRepo, MetadataRepository metadataRepo, ImageService imageService,
      ProductContentService productContentService, MetadataService metadataService,
      ProductMarketplaceDataService productMarketplaceDataService, ExternalDocumentService externalDocumentService,
      ProductMarketplaceDataRepository productMarketplaceDataRepo, VersionService versionService) {
    this.productRepo = productRepo;
    this.productModuleContentRepo = productModuleContentRepo;
    this.axonIvyMarketRepoService = axonIvyMarketRepoService;
    this.axonIvyProductRepoService = axonIvyProductRepoService;
    this.gitHubRepoMetaRepo = gitHubRepoMetaRepo;
    this.gitHubService = gitHubService;
    this.productCustomSortRepo = productCustomSortRepo;
    this.mavenArtifactVersionRepo = mavenArtifactVersionRepo;
    this.metadataSyncRepo = metadataSyncRepo;
    this.metadataRepo = metadataRepo;
    this.productJsonContentRepo = productJsonContentRepo;
    this.imageRepo = imageRepo;
    this.imageService = imageService;
    this.metadataService = metadataService;
    this.productContentService = productContentService;
    this.productMarketplaceDataService = productMarketplaceDataService;
    this.externalDocumentService = externalDocumentService;
    this.productMarketplaceDataRepo = productMarketplaceDataRepo;
    this.versionService = versionService;
  }

  @Override
  public Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient,
      Pageable pageable) {
    final var typeOption = TypeOption.of(type);
    final var searchPageable = refinePagination(language, pageable);
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setListed(true);
    searchCriteria.setKeyword(keyword);
    searchCriteria.setType(typeOption);
    searchCriteria.setLanguage(Language.of(language));
    if (BooleanUtils.isTrue(isRESTClient)) {
      searchCriteria.setExcludeFields(List.of(SHORT_DESCRIPTIONS));
    }
    return productRepo.searchByCriteria(searchCriteria, searchPageable);
  }

  @Override
  public List<String> syncLatestDataFromMarketRepo(Boolean resetSync) {
    List<String> syncedProductIds = new ArrayList<>();
    var isAlreadyUpToDate = false;
    marketRepoMeta = gitHubRepoMetaRepo.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (BooleanUtils.isTrue(resetSync) && marketRepoMeta != null) {
      gitHubRepoMetaRepo.delete(marketRepoMeta);
      marketRepoMeta = null;
    } else {
      isAlreadyUpToDate = isLastGithubCommitCovered();
    }

    if (!isAlreadyUpToDate) {
      if (marketRepoMeta == null) {
        syncedProductIds = syncProductsFromGitHubRepo(resetSync);
        marketRepoMeta = new GitHubRepoMeta();
      } else {
        syncedProductIds = updateLatestChangeToProductsFromGithubRepo();
      }
      syncRepoMetaDataStatus();
    }
    updateLatestReleaseVersionContentsFromProductRepo();
    return syncedProductIds.stream().filter(StringUtils::isNotBlank).toList();
  }

  private void syncRepoMetaDataStatus() {
    if (lastGHCommit == null) {
      return;
    }
    String repoURL = Optional.ofNullable(lastGHCommit.getOwner()).map(GHRepository::getUrl).map(URL::getPath)
        .orElse(EMPTY);
    marketRepoMeta.setRepoURL(repoURL);
    marketRepoMeta.setRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    marketRepoMeta.setLastSHA1(lastGHCommit.getSHA1());
    marketRepoMeta.setLastChange(GitHubUtils.getGHCommitDate(lastGHCommit));
    gitHubRepoMetaRepo.save(marketRepoMeta);
    marketRepoMeta = null;
  }

  private List<String> updateLatestChangeToProductsFromGithubRepo() {
    Set<String> syncedProductIds = new HashSet<>();
    var fromSHA1 = marketRepoMeta.getLastSHA1();
    var toSHA1 = ofNullable(lastGHCommit).map(GHCommit::getSHA1).orElse(EMPTY);
    log.warn("**ProductService: synchronize products from SHA1 {} to SHA1 {}", fromSHA1, toSHA1);
    List<GitHubFile> gitHubFileChanges = axonIvyMarketRepoService.fetchMarketItemsBySHA1Range(fromSHA1, toSHA1);
    Map<String, List<GitHubFile>> groupGitHubFiles = new HashMap<>();
    for (var file : gitHubFileChanges) {
      String filePath = file.getFileName();
      var parentPath = filePath.substring(0, filePath.lastIndexOf(SLASH) + 1);
      var files = groupGitHubFiles.getOrDefault(parentPath, new ArrayList<>());
      files.add(file);
      files.sort((file1, file2) -> GitHubUtils.sortMetaJsonFirst(file1.getFileName(), file2.getFileName()));
      groupGitHubFiles.putIfAbsent(parentPath, files);
    }

    groupGitHubFiles.forEach((key, value) -> {
      for (var file : value) {
        var productId = EMPTY;
        if (file.getStatus() == MODIFIED || file.getStatus() == ADDED) {
          productId = modifyProductMetaOrLogo(file, key);
        } else {
          productId = removeProductAndImage(file);
        }
        syncedProductIds.add(productId);
      }
    });
    return syncedProductIds.stream().toList();
  }

  private String removeProductAndImage(GitHubFile file) {
    String productId = EMPTY;
    if (FileType.META == file.getType()) {
      String[] splitMetaJsonPath = file.getFileName().split(SLASH);
      String extractMarketDirectory = file.getFileName().replace(splitMetaJsonPath[splitMetaJsonPath.length - 1],
          EMPTY);
      List<Product> productList = productRepo.findByMarketDirectory(extractMarketDirectory);
      if (ObjectUtils.isNotEmpty(productList)) {
        productId = productList.get(0).getId();
        productRepo.deleteById(productId);
        imageRepo.deleteAllByProductId(productId);
      }
    } else {
      List<Image> images = imageRepo.findByImageUrlEndsWithIgnoreCase(file.getFileName());
      if (ObjectUtils.isNotEmpty(images)) {
        Image currentImage = images.get(0);
        productId = currentImage.getProductId();
        productRepo.deleteById(productId);
        imageRepo.deleteAllByProductId(productId);
      }
    }
    return productId;
  }

  private String modifyProductMetaOrLogo(GitHubFile file, String parentPath) {
    try {
      GHContent fileContent = gitHubService.getGHContent(axonIvyMarketRepoService.getRepository(), file.getFileName(),
          marketRepoBranch);
      return updateProductByMetaJsonAndLogo(fileContent, file, parentPath);
    } catch (IOException e) {
      log.error("Get GHContent failed: ", e);
    }
    return EMPTY;
  }

  private String updateProductByMetaJsonAndLogo(GHContent fileContent, GitHubFile file, String parentPath) {
    String productId;
    if (FileType.META == file.getType()) {
      Product product = new Product();
      ProductFactory.mappingByGHContent(product, fileContent);
      mappingVendorImageFromGHContent(product, fileContent);
      transferComputedDataFromDB(product);
      productId = productRepo.save(product).getId();
    } else {
      productId = modifyProductLogo(parentPath, fileContent);
    }
    return productId;
  }

  private String modifyProductLogo(String parentPath, GHContent fileContent) {
    var searchCriteria = new ProductSearchCriteria();
    searchCriteria.setKeyword(parentPath);
    searchCriteria.setFields(List.of(MARKET_DIRECTORY));
    Product result = productRepo.findByCriteria(searchCriteria);
    if (result != null) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(result.getId(), fileContent)).ifPresent(image -> {
        if (StringUtils.isNotBlank(result.getLogoId())) {
          imageRepo.deleteById(result.getLogoId());
        }
        result.setLogoId(image.getId());
        productRepo.save(result);
      });
      return result.getId();
    }
    log.info("There is no product to update the logo with path {}", parentPath);
    return EMPTY;
  }

  private Pageable refinePagination(String language, Pageable pageable) {
    PageRequest pageRequest = (PageRequest) pageable;
    if (pageable != null) {
      List<Order> orders = new ArrayList<>();
      for (var sort : pageable.getSort()) {
        SortOption sortOption = SortOption.of(sort.getProperty());
        Order order = createOrder(sortOption, language);
        orders.add(order);
        if (SortOption.STANDARD.equals(sortOption)) {
          orders.add(getExtensionOrder(language));
        }
      }
      Order orderById = createOrder(SortOption.ID, language);
      orders.add(orderById);
      pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
    return pageRequest;
  }

  public Order createOrder(SortOption sortOption, String language) {
    if (SortOption.STANDARD.equals(sortOption)) {
      return new Order(sortOption.getDirection(), MongoDBConstants.MARKETPLACE_DATA_CUSTOM_ORDER);
    }
    return new Order(sortOption.getDirection(), sortOption.getCode(language));
  }

  private Order getExtensionOrder(String language) {
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();

    if (!customSorts.isEmpty()) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      return createOrder(sortOptionExtension, language);
    }
    return createOrder(SortOption.POPULARITY, language);
  }

  private boolean isLastGithubCommitCovered() {
    boolean isLastCommitCovered = false;
    long lastCommitTime = 0L;
    if (marketRepoMeta != null) {
      lastCommitTime = marketRepoMeta.getLastChange();
    }
    lastGHCommit = axonIvyMarketRepoService.getLastCommit(lastCommitTime);
    if (lastGHCommit != null && marketRepoMeta != null && StringUtils.equals(lastGHCommit.getSHA1(),
        marketRepoMeta.getLastSHA1())) {
      isLastCommitCovered = true;
    }
    return isLastCommitCovered;
  }

  private void updateLatestReleaseVersionContentsFromProductRepo() {
    List<Product> products = productRepo.findAll();
    if (ObjectUtils.isEmpty(products)) {
      return;
    }

    for (Product product : products) {
      updateProductFromReleasedVersions(product);
      productRepo.save(product);
    }
  }

  private List<String> syncProductsFromGitHubRepo(Boolean resetSync) {
    log.warn("**ProductService: synchronize products from scratch based on the Market repo");
    List<String> syncedProductIds = new ArrayList<>();
    var gitHubContentMap = axonIvyMarketRepoService.fetchAllMarketItems();
    for (Map.Entry<String, List<GHContent>> ghContentEntity : gitHubContentMap.entrySet()) {
      var product = new Product();
      //update the meta.json first
      ghContentEntity.getValue().sort((f1, f2) -> GitHubUtils.sortMetaJsonFirst(f1.getName(), f2.getName()));

      for (var content : ghContentEntity.getValue()) {
        ProductFactory.mappingByGHContent(product, content);
        mappingVendorImageFromGHContent(product, content);
        mappingLogoFromGHContent(product, content);
      }

      if (BooleanUtils.isTrue(resetSync)) {
        productModuleContentRepo.deleteAllByProductId(product.getId());
        productJsonContentRepo.deleteAllByProductId(product.getId());
      } else if (productRepo.findById(product.getId()).isPresent()) {
        continue;
      }
      updateProductContentForNonStandardProduct(ghContentEntity.getValue(), product);
      updateFirstPublishedDate(product);
      updateProductFromReleasedVersions(product);
      transferComputedDataFromDB(product);
      productMarketplaceDataRepo.checkAndInitProductMarketplaceDataIfNotExist(product.getId());
      syncedProductIds.add(productRepo.save(product).getId());
    }
    return syncedProductIds;
  }

  private void mappingLogoFromGHContent(Product product, GHContent ghContent) {
    if (ghContent != null && StringUtils.endsWith(ghContent.getName(), LOGO_FILE)) {
      Optional.ofNullable(imageService.mappingImageFromGHContent(product.getId(), ghContent))
          .ifPresent(image -> product.setLogoId(image.getId()));
    }
  }

  private void mappingVendorImageFromGHContent(Product product, GHContent ghContent) {
    if (StringUtils.endsWith(ghContent.getName(), MetaConstants.META_FILE)) {
      if (StringUtils.isNotBlank(product.getVendorImagePath())) {
        product.setVendorImage(mapVendorImage(product.getId(), ghContent, product.getVendorImagePath()));
      }
      if (StringUtils.isNotBlank(product.getVendorImageDarkModePath())) {
        product.setVendorImageDarkMode(mapVendorImage(product.getId(), ghContent, product.getVendorImageDarkModePath()));
      }
    }
  }

  private String mapVendorImage(String productId, GHContent ghContent, String imageName) {
    if (StringUtils.isNotBlank(imageName)) {
      String imagePath = StringUtils.replace(ghContent.getPath(), MetaConstants.META_FILE, imageName);
      try {
        GHContent imageContent = gitHubService.getGHContent(ghContent.getOwner(), imagePath, marketRepoBranch);
        return Optional.ofNullable(imageService.mappingImageFromGHContent(productId, imageContent))
            .map(Image::getId).orElse(EMPTY);
      } catch (IOException e) {
        log.error("Get Vendor Image failed: ", e);
      }
    }
    return EMPTY;
  }

  private void updateFirstPublishedDate(Product product) {
    try {
      if (StringUtils.isNotBlank(product.getRepositoryName())) {
        List<GHTag> gitHubTags = gitHubService.getRepositoryTags(product.getRepositoryName());
        Date firstTagPublishedDate = getFirstTagPublishedDate(gitHubTags);
        product.setFirstPublishedDate(firstTagPublishedDate);
      }
    } catch (IOException e) {
      log.error("Get GH Tags failed: ", e);
    }
  }

  private Date getFirstTagPublishedDate(List<GHTag> gitHubTags) {
    Date firstTagPublishedDate = null;
    try {
      if (!CollectionUtils.isEmpty(gitHubTags)) {
        List<GHTag> sortedTags = sortByTagCommitDate(gitHubTags);
        GHCommit commit = sortedTags.get(0).getCommit();
        if (commit != null) {
          firstTagPublishedDate = commit.getCommitDate();
        }
      }
    } catch (IOException e) {
      log.error("Get first tag published date failed: ", e);
    }

    return firstTagPublishedDate;
  }

  private List<GHTag> sortByTagCommitDate(List<GHTag> gitHubTags) {
    List<GHTag> sortedTags = new ArrayList<>(gitHubTags);
    sortedTags.sort(Comparator.comparing(this::sortByCommitDate, Comparator.nullsLast(Comparator.naturalOrder())));
    return sortedTags;
  }

  private Date sortByCommitDate(GHTag gitHubTag) {
    Date commitDate = null;
    try {
      if (gitHubTag.getCommit() != null) {
        commitDate = gitHubTag.getCommit().getCommitDate();
      }
    } catch (IOException e) {
      log.error("Get commit date of tag commit failed: ", e);
    }
    return commitDate;
  }

  private void updateProductFromReleasedVersions(Product product) {
    if (ObjectUtils.isEmpty(product.getArtifacts())) {
      return;
    }

    List<Artifact> productArtifacts = product.getArtifacts().stream()
        .filter(productArtifact -> productArtifact.getArtifactId().contains(MavenConstants.PRODUCT_ARTIFACT_POSTFIX))
        .toList();

    List<Artifact> archivedArtifacts = product.getArtifacts().stream()
        .filter(artifact -> !CollectionUtils.isEmpty(artifact.getArchivedArtifacts()))
        .flatMap(artifact -> artifact.getArchivedArtifacts().stream()
            .map(archivedArtifact -> Artifact.builder()
                .groupId(archivedArtifact.getGroupId())
                .artifactId(archivedArtifact.getArtifactId())
                .build()))
        .toList();

    List<Artifact> mavenArtifacts = new ArrayList<>();
    mavenArtifacts.addAll(productArtifacts);
    mavenArtifacts.addAll(archivedArtifacts);

    List<String> nonSyncReleasedVersions = new ArrayList<>();
    for (Artifact mavenArtifact : mavenArtifacts) {
      getMetadataContent(mavenArtifact, product, nonSyncReleasedVersions);
    }
    metadataService.updateArtifactAndMetadata(product.getId(), nonSyncReleasedVersions, product.getArtifacts());
    externalDocumentService.syncDocumentForProduct(product.getId(), nonSyncReleasedVersions, false);
  }

  private void getMetadataContent(Artifact artifact, Product product, List<String> nonSyncReleasedVersions) {
    String metadataUrl = MavenUtils.buildMetadataUrlFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(),
        createProductArtifactId(artifact));
    String metadataContent = MavenUtils.getMetadataContentFromUrl(metadataUrl);
    if (StringUtils.isNotBlank(metadataContent)) {
      updateContentsFromMavenXML(product, metadataContent, artifact, nonSyncReleasedVersions);
    }
  }

  private void updateContentsFromMavenXML(Product product, String metadataContent, Artifact mavenArtifact,
      List<String> nonSyncReleasedVersions) {
    Document document = MetadataReaderUtils.getDocumentFromXMLContent(metadataContent);

    NodeList versionNodes = document.getElementsByTagName(MavenConstants.VERSION_TAG);
    List<String> mavenVersions = new ArrayList<>();
    for (int i = 0; i < versionNodes.getLength(); i++) {
      mavenVersions.add(versionNodes.item(i).getTextContent());
    }

    // Check if having new released version or unreleased dev version in Maven
    List<String> currentVersions = ObjectUtils.isNotEmpty(product.getReleasedVersions()) ?
        product.getReleasedVersions() :
        productModuleContentRepo.findVersionsByProductId(product.getId());
    List<String> versionChanges =
        mavenVersions.stream().filter(
            version -> !currentVersions.contains(version) || (!VersionUtils.isReleasedVersion(
                version) && VersionUtils.isOfficialVersionOrUnReleasedDevVersion(
                mavenVersions, version))).toList();

    if (ObjectUtils.isEmpty(versionChanges)) {
      return;
    }

    Date lastUpdated = getLastUpdatedDate(document);
    if (ObjectUtils.isEmpty(product.getNewestPublishedDate()) || lastUpdated.after(product.getNewestPublishedDate())) {
      String latestVersion = MetadataReaderUtils.getElementValue(document, MavenConstants.LATEST_VERSION_TAG);
      product.setNewestPublishedDate(lastUpdated);
      product.setNewestReleaseVersion(latestVersion);
    }
    updateProductCompatibility(product, versionChanges);

    Optional.ofNullable(product.getReleasedVersions()).ifPresentOrElse(releasedVersion -> {},
        () -> product.setReleasedVersions(new ArrayList<>()));

    List<ProductModuleContent> productModuleContents = new ArrayList<>();
    for (String version : versionChanges) {
      product.getReleasedVersions().add(version);
      ProductModuleContent productModuleContent = handleProductArtifact(version, product.getId(), mavenArtifact,
          product.getNames().get(EN_LANGUAGE));
      Optional.ofNullable(productModuleContent).ifPresent(productModuleContents::add);
    }
    nonSyncReleasedVersions.addAll(versionChanges);

    if (ObjectUtils.isNotEmpty(productModuleContents)) {
      productModuleContentRepo.saveAll(productModuleContents);
    }
  }

  private Date getLastUpdatedDate(Document document) {
    DateTimeFormatter lastUpdatedFormatter = DateTimeFormatter.ofPattern(MavenConstants.DATE_TIME_FORMAT);
    LocalDateTime newestPublishedDate =
        LocalDateTime.parse(Objects.requireNonNull(MetadataReaderUtils.getElementValue(document,
            MavenConstants.LAST_UPDATED_TAG)), lastUpdatedFormatter);
    return Date.from(newestPublishedDate.atZone(ZoneOffset.UTC).toInstant());
  }

  private void updateProductCompatibility(Product product, List<String> mavenVersions) {
    if (StringUtils.isBlank(product.getCompatibility())) {
      String oldestVersion = VersionUtils.getOldestVersions(mavenVersions);
      if (oldestVersion != null) {
        String compatibility = getCompatibilityFromOldestVersion(oldestVersion);
        product.setCompatibility(compatibility);
      }
    }
  }

  public ProductModuleContent handleProductArtifact(String version, String productId, Artifact mavenArtifact,
      String productName) {
    String snapshotVersionValue = Strings.EMPTY;
    if (version.contains(MavenConstants.SNAPSHOT_VERSION)) {
      snapshotVersionValue = MetadataReaderUtils.getSnapshotVersionValue(version, mavenArtifact);
    }

    String repoUrl = StringUtils.defaultIfBlank(mavenArtifact.getRepoUrl(), DEFAULT_IVY_MAVEN_BASE_URL);
    String artifactId = createProductArtifactId(mavenArtifact);
    String type = StringUtils.defaultIfBlank(mavenArtifact.getType(), DEFAULT_PRODUCT_FOLDER_TYPE);
    String url = MavenUtils.buildDownloadUrl(artifactId, version, type,
        repoUrl, mavenArtifact.getGroupId(), StringUtils.defaultIfBlank(snapshotVersionValue, version));

    return productContentService.getReadmeAndProductContentsFromVersion(productId,
        version, url, mavenArtifact, productName);
  }

  private String createProductArtifactId(Artifact mavenArtifact) {
    return mavenArtifact.getArtifactId().contains(PRODUCT_ARTIFACT_POSTFIX) ? mavenArtifact.getArtifactId()
        : mavenArtifact.getArtifactId().concat(PRODUCT_ARTIFACT_POSTFIX);
  }

  // Cover 3 cases after removing non-numeric characters (8, 11.1 and 10.0.2)
  @Override
  public String getCompatibilityFromOldestVersion(String oldestVersion) {
    if (StringUtils.isBlank(oldestVersion)) {
      return Strings.EMPTY;
    }
    if (!oldestVersion.contains(DOT_SEPARATOR)) {
      return oldestVersion + ".0+";
    }
    int firstDot = oldestVersion.indexOf(DOT_SEPARATOR);
    int secondDot = oldestVersion.indexOf(DOT_SEPARATOR, firstDot + 1);
    if (secondDot == -1) {
      return oldestVersion.concat(PLUS);
    }
    return oldestVersion.substring(0, secondDot).concat(PLUS);
  }

  @Override
  public Product fetchProductDetail(String id, Boolean isShowDevVersion) {
    Product product = getProductByIdWithNewestReleaseVersion(id, isShowDevVersion);
    return Optional.ofNullable(product).map(productItem -> {
      int installationCount = productMarketplaceDataService.updateProductInstallationCount(id);
      productItem.setInstallationCount(installationCount);

      String compatibilityRange = getCompatibilityRange(id);
      productItem.setCompatibilityRange(compatibilityRange);

      return productItem;
    }).orElse(null);
  }

  @Override
  public Product fetchBestMatchProductDetail(String id, String version) {
    List<String> installableVersions = VersionUtils.getInstallableVersionsFromMetadataList(
        metadataRepo.findByProductId(id));
    String bestMatchVersion = VersionUtils.getBestMatchVersion(installableVersions, version);
    // Cover exception case of employee onboarding without any product.json file
    Product product = StringUtils.isBlank(bestMatchVersion) ? getProductByIdWithNewestReleaseVersion(id,
        false) : productRepo.getProductByIdAndVersion(id, bestMatchVersion);
    return Optional.ofNullable(product).map(productItem -> {
      int installationCount = productMarketplaceDataService.updateProductInstallationCount(id);
      productItem.setInstallationCount(installationCount);

      String compatibilityRange = getCompatibilityRange(id);
      productItem.setCompatibilityRange(compatibilityRange);

      productItem.setBestMatchVersion(bestMatchVersion);
      return productItem;
    }).orElse(null);
  }

  public Product getProductByIdWithNewestReleaseVersion(String id, Boolean isShowDevVersion) {
    List<String> versions;
    String version = StringUtils.EMPTY;

    var mavenArtifactVersion = mavenArtifactVersionRepo.findById(id);
    if (mavenArtifactVersion.isPresent()) {
      versions = MavenUtils.getAllExistingVersions(mavenArtifactVersion.get(), BooleanUtils.isTrue(isShowDevVersion),
          StringUtils.EMPTY);
      version = CollectionUtils.firstElement(versions);
    }

    // Cover exception case of employee onboarding without any product.json file
    if (StringUtils.isBlank(version)) {
      versions = VersionUtils.getVersionsToDisplay(productRepo.getReleasedVersionsById(id), isShowDevVersion,
          StringUtils.EMPTY);
      version = CollectionUtils.firstElement(versions);
    }

    Product product = productRepo.getProductByIdAndVersion(id, version);
    productJsonContentRepo.findByProductIdAndVersion(id, version).stream().map(
        ProductJsonContent::getContent).findFirst().ifPresent(
        jsonContent -> product.setMavenDropins(MavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent)));
    return product;
  }

  @Override
  public Product fetchProductDetailByIdAndVersion(String id, String version) {
    return productRepo.getProductByIdAndVersion(id, version);
  }

  public void transferComputedDataFromDB(Product product) {
    productRepo.findById(product.getId()).ifPresent(persistedData ->
        ProductFactory.transferComputedPersistedDataToProduct(persistedData, product)
    );
  }

  @Override
  public boolean syncOneProduct(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
    try {
      log.info("Sync product {} is starting ...", productId);
      log.info("Clean up product {}", productId);
      Product product = renewProductById(productId, marketItemPath, overrideMarketItemPath);
      log.info("Get data of product {} from the git hub", productId);
      var gitHubContents = axonIvyMarketRepoService.getMarketItemByPath(product.getMarketDirectory());
      if (!CollectionUtils.isEmpty(gitHubContents)) {
        log.info("Update data of product {} from meta.json and logo files", productId);
        mappingMetaDataAndLogoFromGHContent(gitHubContents, product);
        updateProductContentForNonStandardProduct(gitHubContents, product);
        updateFirstPublishedDate(product);
        updateProductFromReleasedVersions(product);
        productMarketplaceDataRepo.checkAndInitProductMarketplaceDataIfNotExist(productId);
        productRepo.save(product);
        log.info("Sync product {} is finished!", productId);
        return true;
      }
    } catch (Exception e) {
      log.error(e.getStackTrace());
    }
    return false;
  }

  private Product renewProductById(String productId, String marketItemPath, Boolean overrideMarketItemPath) {
    Product product = new Product();
    productRepo.findById(productId).ifPresent(foundProduct -> {
          ProductFactory.transferComputedPersistedDataToProduct(foundProduct, product);
          imageRepo.deleteAllByProductId(foundProduct.getId());
          metadataRepo.deleteAllByProductId(foundProduct.getId());
          metadataSyncRepo.deleteAllByProductId(foundProduct.getId());
          mavenArtifactVersionRepo.deleteAllById(List.of(foundProduct.getId()));
          productModuleContentRepo.deleteAllByProductId(foundProduct.getId());
          productJsonContentRepo.deleteAllByProductId(foundProduct.getId());
          productRepo.delete(foundProduct);
        }
    );

    if (StringUtils.isNotBlank(marketItemPath) && Boolean.TRUE.equals(overrideMarketItemPath)) {
      product.setMarketDirectory(marketItemPath);
    }
    product.setNewestReleaseVersion(EMPTY);

    return product;
  }

  private void mappingMetaDataAndLogoFromGHContent(List<GHContent> gitHubContent, Product product) {
    var gitHubContents = new ArrayList<>(gitHubContent);
    gitHubContents.sort((f1, f2) -> GitHubUtils.sortMetaJsonFirst(f1.getName(), f2.getName()));
    for (var content : gitHubContent) {
      ProductFactory.mappingByGHContent(product, content);
      mappingVendorImageFromGHContent(product, content);
      mappingLogoFromGHContent(product, content);
    }
  }

  private void updateProductContentForNonStandardProduct(List<GHContent> ghContentEntity,
      Product product) {
    if (StringUtils.isBlank(product.getRepositoryName())) {
      ProductModuleContent initialContent = new ProductModuleContent();
      initialContent.setVersion(INITIAL_VERSION);
      initialContent.setProductId(product.getId());
      ProductFactory.mappingIdForProductModuleContent(initialContent);
      product.setReleasedVersions(List.of(INITIAL_VERSION));
      product.setNewestReleaseVersion(INITIAL_VERSION);
      axonIvyProductRepoService.extractReadMeFileFromContents(product, ghContentEntity, initialContent);
      productModuleContentRepo.save(initialContent);
    }
  }

  @Override
  public boolean syncFirstPublishedDateOfAllProducts() {
    try {
      List<Product> products = productRepo.findAll();
      if (!CollectionUtils.isEmpty(products)) {
        for (Product product : products) {
          if (product.getFirstPublishedDate() == null) {
            log.info("sync FirstPublishedDate of product {} is starting ...", product.getId());
            updateFirstPublishedDate(product);
            productRepo.save(product);
            log.info("Sync FirstPublishedDate of product {} is finished!", product.getId());
          } else {
            log.info("FirstPublishedDate of product {} is existing!", product.getId());
          }
        }
      }
      log.info("sync FirstPublishedDate of all products is finished!");
      return true;
    } catch (Exception e) {
      log.error(e.getStackTrace());
      return false;
    }
  }

  /**
   * MARP-975: Retrieve the list containing all versions for the designer and
   * split the versions to obtain the first prefix,then format them for compatibility range.
   * ex: 11.0+ , 10.0 - 12.0+ , ...
   */
  private String getCompatibilityRange(String productId) {
    return Optional.of(versionService.getVersionsForDesigner(productId))
        .filter(ObjectUtils::isNotEmpty)
        .map(versions -> versions.stream().map(VersionAndUrlModel::getVersion).toList())
        .map(versions -> {
          if (versions.size() == 1) {
            return splitVersion(versions.get(0)).concat(PLUS);
          }
          String maxVersion = splitVersion(versions.get(0)).concat(PLUS);
          String minVersion = splitVersion(versions.get(versions.size() - 1));
          return VersionUtils.getPrefixOfVersion(minVersion).equals(VersionUtils.getPrefixOfVersion(maxVersion)) ?
              minVersion.concat(PLUS) : String.format(COMPATIBILITY_RANGE_FORMAT, minVersion, maxVersion);
        }).orElse(null);
  }

  private String splitVersion(String version) {
    int firstDot = version.indexOf(DOT_SEPARATOR);
    int secondDot = version.indexOf(DOT_SEPARATOR, firstDot + 1);
    return version.substring(0, secondDot);
  }

  @Override
  public Product findProductById(String id) {
    return productRepo.findProductById(id);
  }
}