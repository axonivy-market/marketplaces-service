package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReleaseModel;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHRelease;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;

@RestController
@RequestMapping(PRODUCT_DETAILS)
@Tag(name = "Product Detail Controllers", description = "API collection to get product's detail.")
public class ProductDetailsController {
  private final VersionService versionService;
  private final ProductService productService;
  private final ProductDetailModelAssembler detailModelAssembler;
  private final GitHubService gitHubService;

  public ProductDetailsController(VersionService versionService, ProductService productService,
      ProductDetailModelAssembler detailModelAssembler, GitHubService gitHubService) {
    this.versionService = versionService;
    this.productService = productService;
    this.detailModelAssembler = detailModelAssembler;
    this.gitHubService = gitHubService;
  }

  @GetMapping(BY_ID_AND_VERSION)
  @Operation(summary = "Find product detail by product id and release version.",
      description = "get product detail by it product id and release version")
  public ResponseEntity<ProductDetailModel> findProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Release version (from maven metadata.xml)", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchProductDetailByIdAndVersion(id, version);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BY_ID_AND_VERSION), HttpStatus.OK);
  }

  @GetMapping(BEST_MATCH_BY_ID_AND_VERSION)
  @Operation(summary = "Find best match product detail by product id and version.",
      description = "get product detail by it product id and version")
  public ResponseEntity<ProductDetailModel> findBestMatchProductDetailsByVersion(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @PathVariable(VERSION) @Parameter(description = "Version", example = "10.0.20",
          in = ParameterIn.PATH) String version) {
    var productDetail = productService.fetchBestMatchProductDetail(id, version);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, version, BEST_MATCH_BY_ID_AND_VERSION),
        HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  @Operation(summary = "get product detail by ID", description = "Return product detail by product id (from meta.json)")
  public ResponseEntity<ProductDetailModel> findProductDetails(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "approval-decision-utils",
          in = ParameterIn.PATH) String id,
      @RequestParam(defaultValue = "false", name = SHOW_DEV_VERSION, required = false) @Parameter(description =
          "Option to get Dev Version (Snapshot/ sprint release)", in = ParameterIn.QUERY) Boolean isShowDevVersion) {
    var productDetail = productService.fetchProductDetail(id, isShowDevVersion);
    if (productDetail == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(detailModelAssembler.toModel(productDetail, BY_ID), HttpStatus.OK);
  }

  @GetMapping(VERSIONS_BY_ID)
  public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "adobe-acrobat-connector",
          in = ParameterIn.PATH) String id,
      @RequestParam(SHOW_DEV_VERSION) @Parameter(description = "Option to get Dev Version (Snapshot/ sprint release)",
          in = ParameterIn.QUERY) boolean isShowDevVersion,
      @RequestParam(name = DESIGNER_VERSION, required = false) @Parameter(in = ParameterIn.QUERY,
          example = "v10.0.20") String designerVersion) {
    List<MavenArtifactVersionModel> models =
        versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
    return new ResponseEntity<>(models, HttpStatus.OK);
  }

  @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
  @Operation(summary = "Get product json content for designer to install",
      description = "When we click install in designer, this API will send content of product json for installing in " +
          "Ivy designer")
  public ResponseEntity<Map<String, Object>> findProductJsonContent(@PathVariable(ID) String productId,
      @PathVariable(VERSION) String version) {
    Map<String, Object> productJsonContent = versionService.getProductJsonContentByIdAndVersion(productId, version);
    return new ResponseEntity<>(productJsonContent, HttpStatus.OK);
  }

  @GetMapping(VERSIONS_IN_DESIGNER)
  @Operation(summary = "Get the list of released version in product",
      description = "Collect the released versions in product for ivy designer")
  public ResponseEntity<List<VersionAndUrlModel>> findVersionsForDesigner(@PathVariable(ID) String id) {
    List<VersionAndUrlModel> versionList = versionService.getVersionsForDesigner(id);
    return new ResponseEntity<>(versionList, HttpStatus.OK);
  }

  @GetMapping(LATEST_ARTIFACT_DOWNLOAD_URL_BY_ID)
  @Operation(summary = "Get the download url of latest version from artifact by its id and target version",
      description = "Return the download url of artifact from version and id")
  public ResponseEntity<String> getLatestArtifactDownloadUrl(
      @PathVariable(value = ID) @Parameter(in = ParameterIn.PATH, example = "demos-app") String productId,
      @RequestParam(value = VERSION) @Parameter(in = ParameterIn.QUERY, example = "10.0-dev") String version,
      @RequestParam(value = ARTIFACT) @Parameter(in = ParameterIn.QUERY,
          example = "ivy-demos-app.zip") String artifactId) {
    String downloadUrl = versionService.getLatestVersionArtifactDownloadUrl(productId, version, artifactId);
    HttpStatusCode statusCode = StringUtils.isBlank(downloadUrl) ? HttpStatus.NOT_FOUND : HttpStatus.OK;
    return new ResponseEntity<>(downloadUrl, statusCode);
  }

  @GetMapping(PRODUCT_PUBLIC_RELEASES)
  @Operation(summary = "Get the list of public releases changelog by its id",
      description = "Return the list of public releases changelog and id")
  public ResponseEntity<List<GithubReleaseModel>> getGithubPublicReleases(
      @PathVariable(value = ID) @Parameter(in = ParameterIn.PATH, example = "demos-app") String productId) throws IOException {
    Product product = productService.findProductById(productId);

    List<GithubReleaseModel> githubReleaseModels =
        gitHubService.getReleases(product);

    return new ResponseEntity<>(githubReleaseModels,
        HttpStatus.OK);
  }
}
