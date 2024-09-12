package com.axonivy.market.util;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.hateoas.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ImageUtils {
  public static ProductModuleContent mappingImageForProductModuleContent(ProductModuleContent productModuleContent) {
    mappingImageUrl(productModuleContent.getDescription());
    mappingImageUrl(productModuleContent.getDemo());
    mappingImageUrl(productModuleContent.getSetup());
    return productModuleContent;
  }

  private static void mappingImageUrl(Map<String, String> content){
    content.forEach((key , value ) -> {
      List<String> imageIds = extractAllImageId(value);
      for (String imageId : imageIds) {
        String rawId = imageId.replace("imageId-","");
        Link link = linkTo(methodOn(ProductDetailsController.class).getImageFromId(rawId)).withSelfRel();
        value = value.replace(imageId,link.getHref());
      }
      content.put(key,value);
    });
  }

  private static List<String> extractAllImageId(String content) {
    List<String> result = new ArrayList<>();
    Pattern pattern = Pattern.compile("imageId-\\w+");
    Matcher matcher = pattern.matcher(content);
    while (matcher.find()) {
      var foundImgTag = matcher.group();
      result.add(foundImgTag);
    }
    return result;
  }
}
