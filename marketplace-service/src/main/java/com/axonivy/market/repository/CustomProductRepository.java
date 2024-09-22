package com.axonivy.market.repository;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface CustomProductRepository {
  Product getProductByIdAndTag(String id, String tag);

  Product getProductById(String id);

  List<String> getReleasedVersionsById(String id);

  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);

  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);

  List<String> getAllProductId(String id);
}
