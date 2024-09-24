package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

  private static final String DUMMY_TAG = "v1.0.0";
  public static final String RELEASE_TAG = "v10.0.0";
  public static final String IMAGE_NAME = "image.png";
  public static final String DOCUWARE_CONNECTOR_PRODUCT = "docuware-connector-product";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  PagedIterable<GHTag> listTags;

  @Mock
  GHRepository ghRepository;

  @Mock
  GitHubService gitHubService;

  GHOrganization mockGHOrganization = mock(GHOrganization.class);

  @Mock
  JsonNode dataNode;

  @Mock
  JsonNode childNode;

  @Mock
  GHContent content = new GHContent();

  @Mock
  ImageService imageService;

  @Mock
  ProductJsonContentRepository productJsonContentRepository;

  @InjectMocks
  @Spy
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  void setup() throws IOException {
    when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
  }

  @AfterEach
  void after() {
    reset(mockGHOrganization);
    reset(gitHubService);
  }

  @Test
  void testAllTagsFromRepoName() throws IOException {
    setup();
    var mockTag = mock(GHTag.class);
    when(mockTag.getName()).thenReturn(DUMMY_TAG);
    when(listTags.toList()).thenReturn(List.of(mockTag));
    when(ghRepository.listTags()).thenReturn(listTags);
    var result = axonivyProductRepoServiceImpl.getAllTagsFromRepoName("");
    assertEquals(1, result.size());
    assertEquals(DUMMY_TAG, result.get(0).getName());
  }

  @Test
  void testContentFromGHRepoAndTag() throws IOException {
    setup();
    var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
    assertNull(result);
    when(axonivyProductRepoServiceImpl.getOrganization()).thenThrow(IOException.class);
    result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
    assertNull(result);
  }

  @Test
  void testExtractMavenArtifactFromJsonNode() throws JsonProcessingException {
    List<Artifact> artifacts = new ArrayList<>();
    // Arrange
    String mockJsonNode = """
        {
          "dependencies": [
            {
              "groupId": "com.axonivy.market",
              "artifactId": "octopus-util",
              "version": "1.0.0"
            },
            {
              "groupId": "com.axonivy.market",
              "artifactId": "octopus-util-demo",
              "version": "1.0.0"
            }
          ]
        }
        """;

    JsonNode dataNode = objectMapper.readTree(mockJsonNode);
    boolean isDependency = true;

    MavenUtils.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);

    assertEquals(2, artifacts.size());  // Assert that 2 artifacts were added
    assertEquals("octopus-util", artifacts.get(0).getArtifactId());  // Validate first artifact
    assertEquals("octopus-util-demo", artifacts.get(1).getArtifactId());
  }

  private static GHContent createMockProductJson() {
    GHContent mockProductJson = mock(GHContent.class);
    when(mockProductJson.isFile()).thenReturn(true);
    when(mockProductJson.getName()).thenReturn(ProductJsonConstants.PRODUCT_JSON_FILE, IMAGE_NAME);
    return mockProductJson;
  }

  @Test
  void testCreateArtifactFromJsonNode() {
    String repoUrl = "http://example.com/repo";
    boolean isDependency = true;
    String groupId = "com.example";
    String artifactId = "example-artifact";
    String type = "jar";

    JsonNode groupIdNode = Mockito.mock(JsonNode.class);
    JsonNode artifactIdNode = Mockito.mock(JsonNode.class);
    JsonNode typeNode = Mockito.mock(JsonNode.class);
    Mockito.when(groupIdNode.asText()).thenReturn(groupId);
    Mockito.when(artifactIdNode.asText()).thenReturn(artifactId);
    Mockito.when(typeNode.asText()).thenReturn(type);
    Mockito.when(dataNode.path(ProductJsonConstants.GROUP_ID)).thenReturn(groupIdNode);
    Mockito.when(dataNode.path(ProductJsonConstants.ARTIFACT_ID)).thenReturn(artifactIdNode);
    Mockito.when(dataNode.path(ProductJsonConstants.TYPE)).thenReturn(typeNode);

    Artifact artifact = MavenUtils.createArtifactFromJsonNode(dataNode, repoUrl, isDependency);

    assertEquals(repoUrl, artifact.getRepoUrl());
    assertTrue(artifact.getIsDependency());
    assertEquals(groupId, artifact.getGroupId());
    assertEquals(artifactId, artifact.getArtifactId());
    assertEquals(type, artifact.getType());
    assertTrue(artifact.getIsProductArtifact());
  }

  private static void getReadmeInputStream(String readmeContentString, GHContent mockContent) throws IOException {
    InputStream mockReadmeInputStream = mock(InputStream.class);
    when(mockContent.read()).thenReturn(mockReadmeInputStream);
    when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());
  }

  @Test
  void testGetOrganization() throws IOException {
    Mockito.when(gitHubService.getOrganization(Mockito.anyString())).thenReturn(mockGHOrganization);
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
  }

//  @Test
//  void testGetReadmeAndProductContentsFromTag() throws IOException {
//    String readmeContentWithImage = """
//        #Product-name
//        Test README
//        ## Demo
//        Demo content
//        ## Setup
//        Setup content (image.png)
//        """;
//    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithImage);
//    String readmeContentWithoutHashProductName = """
//        Test README
//        ## Demo
//        Demo content
//        ## Setup
//        Setup content (image.png)
//        """;
//    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithoutHashProductName);
//  }

  private void testGetReadmeAndProductContentsFromTagWithReadmeText(String readmeContentWithImage) throws IOException {
    try (MockedStatic<GitHubUtils> mockedGitHubUtils = Mockito.mockStatic(GitHubUtils.class)) {
      InputStream inputStream = getMockInputStream();
      //Mock readme content
      GHContent mockContent = mock(GHContent.class);
      when(mockContent.isDirectory()).thenReturn(true);
      when(mockContent.isFile()).thenReturn(true);
      when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);
      getReadmeInputStream(readmeContentWithImage, mockContent);

      //Mock product.json content
      GHContent mockContent2 = createMockProductJson();
      when(mockContent2.read()).thenReturn(inputStream);
      mockedGitHubUtils.when(() -> GitHubUtils.extractedContentStream(mockContent2)).thenReturn(
          inputStream);

      when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(
          List.of(mockContent, mockContent2));
      when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(
          List.of(mockContent, mockContent2));
      when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());
      var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
          RELEASE_TAG);

      assertEquals(RELEASE_TAG, result.getTag());
      assertTrue(result.getIsDependency());
      assertEquals("com.axonivy.utils.bpmnstatistic", result.getGroupId());
      assertEquals("bpmn-statistic", result.getArtifactId());
      assertEquals("iar", result.getType());
      assertEquals("Test README", result.getDescription().get(Language.EN.getValue()));
      assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
      assertEquals("Setup content (imageId-66e2b14868f2f95b2f95549a)", result.getSetup().get(Language.EN.getValue()));
    }
  }

  public static Image mockImage() {
    Image image = new Image();
    image.setId("66e2b14868f2f95b2f95549a");
    image.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    image.setProductId("amazon-comprehend");
    image.setImageUrl(
        "https://raw.githubusercontent.com/amazon-comprehend-connector-product/images/comprehend-demo-sentiment.png");
    return image;
  }

  @Test
  void testGetReadmeAndProductContentFromTag_ImageFromFolder() throws IOException {
    String readmeContentWithImageFolder = """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (./images/image.png)
        """;

    GHContent mockImageFile = mock(GHContent.class);
    when(mockImageFile.getName()).thenReturn(ReadmeConstants.IMAGES, IMAGE_NAME);
    when(mockImageFile.isDirectory()).thenReturn(true);
    Mockito.when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());
    PagedIterable<GHContent> pagedIterable = Mockito.mock(String.valueOf(GHContent.class));
    when(mockImageFile.listDirectoryContent()).thenReturn(pagedIterable);
    when(pagedIterable.toList()).thenReturn(List.of(mockImageFile));

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(createMockProduct(),
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals("""
            #Product-name
            Test README
            ## Demo
            Demo content
            ## Setup
            Setup content (imageId-66e2b14868f2f95b2f95549a)
            """,
        updatedReadme);
  }

  @Test
  void testGetReadmeAndProductContentsFromTag_WithNoFullyThreeParts() throws IOException {
    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content";
    GHContent mockContent = createMockProductFolder();
    getReadmeInputStream(readmeContentString, mockContent);

    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);

    assertNull(result.getArtifactId());
    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
  }

  @Test
  void testGetReadmeAndProductContentsFromTag_SwitchPartsPosition() throws IOException {
    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content\n## Demo\nDemo content";
    GHContent mockContent = createMockProductFolder();
    getReadmeInputStream(readmeContentString, mockContent);

    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);
    assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
  }

  @Test
  void testConvertProductJsonToMavenProductInfo() throws IOException {
    assertEquals(0, GitHubUtils.convertProductJsonToMavenProductInfo(null).size());
    assertEquals(0, GitHubUtils.convertProductJsonToMavenProductInfo(content).size());

    InputStream inputStream = getMockInputStream();
    Mockito.when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(2, GitHubUtils.convertProductJsonToMavenProductInfo(content).size());
    inputStream = getMockInputStreamWithOutProjectAndDependency();
    Mockito.when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(0, GitHubUtils.convertProductJsonToMavenProductInfo(content).size());
  }

  private static InputStream getMockInputStream() {
    String jsonContent = getMockProductJsonContent();
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  private static String getMockProductJsonContent() {
    return """
        {
           "$schema": "https://json-schema.axonivy.com/market/10.0.0/product.json",
           "installers": [
             {
               "id": "maven-import",
               "data": {
                 "projects": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic-demo",
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             },
             {
               "id": "maven-dependency",
               "data": {
                 "dependencies": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic",
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             }
           ]
         }
        """;
  }

  private static InputStream getMockInputStreamWithOutProjectAndDependency() {
    String jsonContent = """
        {
          "installers": [
            {
              "data": {
                "repositories": [
                  {
                    "url": "http://example.com/repo"
                  }
                ]
              }
            }
          ]
        }
        """;
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  private Product createMockProduct() {
    Map<String, String> names = Map.of("en", "docuware-connector-name");
    Product product = new Product();
    product.setId("docuware-connector");
    product.setNames(names);
    product.setLanguage("en");
    return product;
  }

  private GHContent createMockProductFolder() throws IOException {
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.isDirectory()).thenReturn(true);
    when(mockContent.isFile()).thenReturn(true);
    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(List.of(mockContent));
    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(List.of(mockContent));

    return mockContent;
  }

  private GHContent createMockProductFolderWithProductJson() throws IOException {
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.isDirectory()).thenReturn(true);
    when(mockContent.isFile()).thenReturn(true);
    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

    GHContent mockContent2 = createMockProductJson();

    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(
        List.of(mockContent, mockContent2));
    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(
        List.of(mockContent, mockContent2));

    return mockContent;
  }

  @Test
  void testExtractedContentStream() {
    assertNull(GitHubUtils.extractedContentStream(null));
    assertNull(GitHubUtils.extractedContentStream(content));
  }

  @Test
  void test_updateDependencyContentsFromProductJson() throws IOException {
    try (MockedStatic<GitHubUtils> mockedGitHubUtils = Mockito.mockStatic(GitHubUtils.class)) {
      ArgumentCaptor<ProductJsonContent> argumentCaptor = ArgumentCaptor.forClass(ProductJsonContent.class);
      String readmeContentWithImage = """
          #Product-name
          Test README
          ## Demo
          Demo content
          ## Setup
          Setup content (image.png)
          """;

      GHContent mockContent = mock(GHContent.class);
      when(mockContent.isDirectory()).thenReturn(true);
      when(mockContent.isFile()).thenReturn(true);
      when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

      GHContent mockContent2 = createMockProductJson();

      when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(
          List.of(mockContent, mockContent2));
      when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(
          List.of(mockContent, mockContent2));
      InputStream inputStream = getMockInputStream();
      when(mockContent.read()).thenReturn(inputStream);
      mockedGitHubUtils.when(() -> GitHubUtils.extractedContentStream(mockContent2)).thenReturn(inputStream);

      axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
          RELEASE_TAG);

      verify(productJsonContentRepository, times(1)).save(argumentCaptor.capture());
      assertEquals("docuware-connector-name", argumentCaptor.getValue().getName());
      assertEquals("10.0.0", argumentCaptor.getValue().getVersion());
      assertEquals("docuware-connector", argumentCaptor.getValue().getProductId());
      assertEquals(getMockProductJsonContent().replace("${version}", "10.0.0"), argumentCaptor.getValue().getContent());
    }
  }
}
