package com.axonivy.market.util;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.model.ReadmeContentsModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

public class ProductContentUtils {
  public static final String DEMO_SETUP_TITLE = "(?i)## Demo|## Setup";
  private static final String HASH = "#";
  public static final String DESCRIPTION = "description";
  public static final String DEMO = "demo";
  public static final String SETUP = "setup";
  public static final String README_IMAGE_FORMAT = "\\(([^)]*?%s[^)]*?)\\)";
  public static final String IMAGE_DOWNLOAD_URL_FORMAT = "(%s)";

  private ProductContentUtils() {
  }

  /**
   * MARP-810: Sabine requires that content in other languages, which has not been translated, be left empty and
   * replaced with English content.
   */
  public static Map<String, String> replaceEmptyContentsWithEnContent(Map<String, String> map) {
    String enValue = map.get(Language.EN.getValue());
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (StringUtils.isBlank(entry.getValue())) {
        map.put(entry.getKey(), enValue);
      }
    }
    return map;
  }

  public static String getReadmeFileLocale(String readmeFile) {
    String result = StringUtils.EMPTY;
    Pattern pattern = Pattern.compile(GitHubConstants.README_FILE_LOCALE_REGEX);
    Matcher matcher = pattern.matcher(readmeFile);
    if (matcher.find()) {
      result = matcher.group(1);
    }
    return result;
  }

  // Cover some cases including when demo and setup parts switch positions or
  // missing one of them
  public static ReadmeContentsModel getExtractedPartsOfReadme( String readmeContents) {
    String[] parts = readmeContents.split(DEMO_SETUP_TITLE);
    int demoIndex = readmeContents.indexOf(ReadmeConstants.DEMO_PART);
    int setupIndex = readmeContents.indexOf(ReadmeConstants.SETUP_PART);
    String description = Strings.EMPTY;
    String setup = Strings.EMPTY;
    String demo = Strings.EMPTY;

    if (parts.length > 0) {
      description = removeFirstLine(parts[0]);
    }

    if (demoIndex != -1 && setupIndex != -1) {
      if (demoIndex < setupIndex) {
        demo = parts[1];
        setup = parts[2];
      } else {
        setup = parts[1];
        demo = parts[2];
      }
    } else if (demoIndex != -1) {
      demo = parts[1];
    } else if (setupIndex != -1) {
      setup = parts[1];
    }

    ReadmeContentsModel readmeContentsModel = new ReadmeContentsModel();
    readmeContentsModel.setDescription(description.trim());
    readmeContentsModel.setDemo(demo.trim());
    readmeContentsModel.setSetup(setup.trim());

    return readmeContentsModel;
  }

  public static boolean hasImageDirectives(String readmeContents) {
    Pattern pattern = Pattern.compile(CommonConstants.IMAGE_EXTENSION);
    Matcher matcher = pattern.matcher(readmeContents);
    return matcher.find();
  }

  public static String removeFirstLine(String text) {
    String result;
    if (StringUtils.isBlank(text)) {
      result = Strings.EMPTY;
    } else if (text.startsWith(HASH)) {
      int index = text.indexOf(StringUtils.LF);
      result = index != StringUtils.INDEX_NOT_FOUND ? text.substring(index + 1).trim() : Strings.EMPTY;
    } else {
      result = text;
    }

    return result;
  }

  public static ProductModuleContent initProductModuleContent(String productId, String version) {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setProductId(productId);
    productModuleContent.setVersion(version);
    ProductFactory.mappingIdForProductModuleContent(productModuleContent);
    return productModuleContent;
  }

  public static void updateProductModule(ProductModuleContent productModuleContent, List<Artifact> artifacts) {
    Artifact artifact = artifacts.stream().filter(Artifact::getIsDependency).findFirst().orElse(null);
    if (Objects.nonNull(artifact)) {
      productModuleContent.setIsDependency(Boolean.TRUE);
      productModuleContent.setGroupId(artifact.getGroupId());
      productModuleContent.setArtifactId(artifact.getArtifactId());
      productModuleContent.setType(StringUtils.defaultIfBlank(artifact.getType(), DEFAULT_PRODUCT_TYPE));
      productModuleContent.setName(artifact.getName());
    }
  }

  public static void updateProductModuleTabContents(ProductModuleContent productModuleContent,
      Map<String, Map<String, String>> moduleContents) {
    productModuleContent.setDescription(replaceEmptyContentsWithEnContent(moduleContents.get(DESCRIPTION)));
    productModuleContent.setDemo(replaceEmptyContentsWithEnContent(moduleContents.get(DEMO)));
    productModuleContent.setSetup(replaceEmptyContentsWithEnContent(moduleContents.get(SETUP)));
  }

  public static String replaceImageDirWithImageCustomId(Map<String, String> imageUrls, String readmeContents) {
    for (Map.Entry<String, String> entry : imageUrls.entrySet()) {
      String imagePattern = String.format(README_IMAGE_FORMAT, Pattern.quote(entry.getKey()));
      readmeContents = readmeContents.replaceAll(imagePattern,
          String.format(IMAGE_DOWNLOAD_URL_FORMAT, entry.getValue()));
    }
    return readmeContents;
  }

  public static void mappingDescriptionSetupAndDemo(Map<String, Map<String, String>> moduleContents,
      String readmeFileName, ReadmeContentsModel readmeContentsModel) {
    String locale = getReadmeFileLocale(readmeFileName);
    locale = StringUtils.isEmpty(locale) ? Language.EN.getValue() : locale.toLowerCase();

    moduleContents.computeIfAbsent(DESCRIPTION, key -> new HashMap<>()).put(locale,
        readmeContentsModel.getDescription());
    moduleContents.computeIfAbsent(SETUP, key -> new HashMap<>()).put(locale, readmeContentsModel.getSetup());
    moduleContents.computeIfAbsent(DEMO, key -> new HashMap<>()).put(locale, readmeContentsModel.getDemo());
  }
}
