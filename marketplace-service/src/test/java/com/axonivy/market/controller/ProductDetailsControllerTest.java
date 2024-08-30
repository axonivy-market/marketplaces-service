package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;
import com.axonivy.market.constants.RequestMappingConstants;
import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.service.ProductDesignerInstallationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
  public static final String TAG = "v10.0.6";
  @Mock
  private ProductService productService;

  @Mock
  VersionService versionService;

  @Mock
  ProductDesignerInstallationService productDesignerInstallationService;

  @Mock
  private ProductDetailModelAssembler detailModelAssembler;

  @InjectMocks
  private ProductDetailsController productDetailsController;
  private static final String PRODUCT_NAME_SAMPLE = "Docker";
  private static final String PRODUCT_NAME_DE_SAMPLE = "Docker DE";
  public static final String DOCKER_CONNECTOR_ID = "docker-connector";

  @Test
  void testProductDetails() {
    Mockito.when(productService.fetchProductDetail(Mockito.anyString())).thenReturn(mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), RequestMappingConstants.BY_ID)).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails(DOCKER_CONNECTOR_ID);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetail(DOCKER_CONNECTOR_ID);
    verify(detailModelAssembler, times(1)).toModel(mockProduct(), RequestMappingConstants.BY_ID);
    assertTrue(result.hasBody());
    assertEquals(DOCKER_CONNECTOR_ID, Objects.requireNonNull(result.getBody()).getId());
  }


  @Test
  void testFindBestMatchProductDetailsByVersion() {
    Mockito.when(productService.fetchBestMatchProductDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), TAG, RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION)).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findBestMatchProductDetailsByVersion(DOCKER_CONNECTOR_ID, TAG);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchBestMatchProductDetail(DOCKER_CONNECTOR_ID, TAG);
    verify(detailModelAssembler, times(1)).toModel(mockProduct(), TAG, RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION);
  }

  @Test
  void testProductDetailsWithVersion() {
    Mockito.when(productService.fetchProductDetailByIdAndVersion(Mockito.anyString(), Mockito.anyString())).thenReturn(mockProduct());
    Mockito.when(detailModelAssembler.toModel(mockProduct(), TAG, RequestMappingConstants.BY_ID_AND_VERSION)).thenReturn(createProductMockWithDetails());
    ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
        HttpStatus.OK);

    ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetailsByVersion(
        DOCKER_CONNECTOR_ID, TAG);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(result, mockExpectedResult);

    verify(productService, times(1)).fetchProductDetailByIdAndVersion(DOCKER_CONNECTOR_ID, TAG);
  }

  @Test
  void testFindProductVersionsById() {
    List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
    Mockito.when(
            versionService.getArtifactsAndVersionToDisplay(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString()))
        .thenReturn(models);
    ResponseEntity<List<MavenArtifactVersionModel>> result = productDetailsController.findProductVersionsById("portal",
        true, "10.0.1");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals(models, result.getBody());
  }

  @Test
  void testSyncInstallationCount() {
    when(productService.updateInstallationCountForProduct("google-maps-connector")).thenReturn(1);

    var result = productDetailsController.syncInstallationCount("google-maps-connector");

    assertEquals(1, result.getBody());
  }

  @Test
  void findProductVersionsById() {
    when(versionService.getVersionsForDesigner("google-maps-connector")).thenReturn(
        List.of("10.0.21", "10.0.22", "10.0.23"));

    var result = productDetailsController.findVersionsForDesigner("google-maps-connector");

    assertEquals(3, Objects.requireNonNull(result.getBody()).size());
    assertEquals("10.0.21", Objects.requireNonNull(result.getBody()).get(0));
    assertEquals("10.0.22", Objects.requireNonNull(result.getBody()).get(1));
    assertEquals("10.0.23", Objects.requireNonNull(result.getBody()).get(2));
  }

  @Test
  void findProductJsonContentByIdAndTag() throws IOException {
    ProductJsonContent productJsonContent = mockProductJsonContent();
    Map<String , Object> map = new ObjectMapper().readValue(productJsonContent.getContent(), Map.class);
    when(versionService.getProductJsonContentByIdAndVersion("bpmnstatistic", "10.0.21")).thenReturn(
        map);

    var result = productDetailsController.findProductJsonContent("bpmnstatistic", "10.0.21");

    assertEquals(new ResponseEntity<>(map, HttpStatus.OK), result);
  }

  private Product mockProduct() {
    Product mockProduct = new Product();
    mockProduct.setId(DOCKER_CONNECTOR_ID);
    Map<String, String> name = new HashMap<>();
    name.put(Language.EN.getValue(), PRODUCT_NAME_SAMPLE);
    name.put(Language.DE.getValue(), PRODUCT_NAME_DE_SAMPLE);
    mockProduct.setNames(name);
    mockProduct.setLanguage("English");
    return mockProduct;
  }

  private ProductDetailModel createProductMockWithDetails() {
    ProductDetailModel mockProductDetail = new ProductDetailModel();
    mockProductDetail.setId(DOCKER_CONNECTOR_ID);
    Map<String, String> name = new HashMap<>();
    name.put(Language.EN.getValue(), PRODUCT_NAME_SAMPLE);
    name.put(Language.DE.getValue(), PRODUCT_NAME_DE_SAMPLE);
    mockProductDetail.setNames(name);
    mockProductDetail.setType("connector");
    mockProductDetail.setCompatibility("10.0+");
    mockProductDetail.setSourceUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
    mockProductDetail.setLanguage("English");
    mockProductDetail.setIndustry("Cross-Industry");
    mockProductDetail.setContactUs(false);
    return mockProductDetail;
  }

  private ProductJsonContent mockProductJsonContent() {
    String encodedContent = """
        {
            "$schema": "https://json-schema.axonivy.com/market/10.0.0/product.json",
            "minimumIvyVersion": "10.0.8",
            "installers": [
                {
                    "id": "maven-import",
                    "data": {
                        "projects": [
                            {
                                "groupId": "com.axonivy.utils.docfactory",
                                "artifactId": "aspose-barcode-demo",
                                "version": "${version}",
                                "type": "iar"
                            }
                        ],
                        "repositories": [
                            {
                                "id": "maven.axonivy.com",
                                "url": "https://maven.axonivy.com"
                            }
                        ]
                    }
                }
            ]
        }
        """;

    ProductJsonContent jsonContent = new ProductJsonContent();
    jsonContent.setContent(encodedContent);
    jsonContent.setName("aspose-barcode");

    return jsonContent;
  }

  @Test
  void testIncreaseDesignerInstallationCount() {
    Mockito.when(productService.increaseInstallationCountForProductByDesignerVersion(Mockito.anyString(),
            Mockito.anyString())).thenReturn(Boolean.TRUE);
    ResponseEntity<Boolean> result = productDetailsController.increaseDesignerInstallationCount("portal", "11.4.0");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(Boolean.TRUE, result.getBody());
  }

  @Test
  void testGetProductDesignerInstallationByProductId() {
    List<DesignerInstallation> models = List.of(new DesignerInstallation("11.4.0", 5));
    Mockito.when(productDesignerInstallationService.findByProductId(Mockito.anyString())).thenReturn(models);
    ResponseEntity<List<DesignerInstallation>> result = productDetailsController.getProductDesignerInstallationByProductId("portal");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals("11.4.0", result.getBody().get(0).getDesignerVersion());
    Assertions.assertEquals(5, result.getBody().get(0).getNumberOfDownloads());
    Assertions.assertEquals(models, result.getBody());
  }
}
