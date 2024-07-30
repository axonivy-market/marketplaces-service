package com.axonivy.market.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, ProductListedRepository {

  Page<Product> findByType(String type, Pageable pageable);

  Product findByLogoUrl(String logoUrl);

  @Override
  Optional<Product> findById(String productId);

  @Query("{'marketDirectory': {$regex : ?0, $options: 'i'}}")
  Product findByMarketDirectoryRegex(String search);

  @Query("{ $and: [ { $or: [ { 'names.?': { $regex: ?0, $options: 'i' } }, { 'shortDescriptions.?': { $regex: ?0, $options: 'i' } } ] }, { 'type': ?1 } ] }")
  Page<Product> searchByKeywordAndType(String keyword, String type, String language, Pageable unifiedPageabe);

  @Query("{ $or: [ { 'names.?1': { $regex: ?0, $options: 'i' } }, { 'shortDescriptions.?1': { $regex: ?0, $options: 'i' } } ] }")
  Page<Product> searchByNameOrShortDescriptionRegex(String keyword, String language, Pageable unifiedPageabe);

  @Query("{ $and: [{ $or: [ { 'names.en': { $regex: ?0 , $options: 'i' } }] },{ 'type': 'connector' }]}")
  Page<Product> searchByNameAndType(String search, Pageable unifiedPageable);
}
