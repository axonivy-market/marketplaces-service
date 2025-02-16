package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.CustomRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.enums.DocumentField.LISTED;
import static com.axonivy.market.enums.DocumentField.TYPE;

@Builder
@AllArgsConstructor
public class CustomProductRepositoryImpl extends CustomRepository implements CustomProductRepository {
  public static final String CASE_INSENSITIVITY_OPTION = "i";
  public static final String LOCALIZE_SEARCH_PATTERN = "%s.%s";

  final MongoTemplate mongoTemplate;
  final ProductModuleContentRepository contentRepository;

  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  public List<Product> queryProductsByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getMappedResults).orElse(Collections.emptyList());
  }

  @Override
  public Product getProductByIdAndVersion(String id, String version) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByVersionAndProductId(version, id);
      result.setProductModuleContent(content);
    }
    return result;
  }

  @Override
  public Product findProductById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    Product product = queryProductByAggregation(aggregation);
    if (Objects.isNull(product)) {
      return Collections.emptyList();
    }
    return product.getReleasedVersions();
  }

  @Override
  public List<Product> getAllProductsWithIdAndReleaseTagAndArtifact() {
    return queryProductsByAggregation(
        createProjectIdAndReleasedVersionsAndArtifactsAggregation());
  }

  protected Aggregation createProjectIdAndReleasedVersionsAndArtifactsAggregation() {
    return Aggregation.newAggregation(
        Aggregation.project(MongoDBConstants.ID, MongoDBConstants.ARTIFACTS, MongoDBConstants.RELEASED_VERSIONS)
    );
  }

  @Override
  public Page<Product> searchByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    return getResultAsPageable(pageable, buildCriteriaSearch(searchCriteria));
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    Criteria searchCriteria = buildCriteriaSearch(criteria);
    List<Product> entities = mongoTemplate.find(new Query(searchCriteria), Product.class);
    return CollectionUtils.isEmpty(entities) ? null : entities.get(0);
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    var criteria = new Criteria();
    criteria.andOperator(Criteria.where(MongoDBConstants.ARTIFACTS_DOC).is(true));
    return mongoTemplate.find(new Query(criteria), Product.class);
  }

  private Page<Product> getResultAsPageable(Pageable pageable, Criteria criteria) {
    int skip = (int) pageable.getOffset();
    int limit = pageable.getPageSize();
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.match(criteria),
        Aggregation.lookup(MongoDBConstants.PRODUCT_MARKETPLACE_COLLECTION, MongoDBConstants.ID, MongoDBConstants.ID,
            MongoDBConstants.MARKETPLACE_DATA),
        Aggregation.sort(pageable.getSort()),
        Aggregation.skip(skip),
        Aggregation.limit(limit)
    );

    List<Product> entities = mongoTemplate.aggregate(aggregation, MongoDBConstants.PRODUCT_COLLECTION,
        Product.class).getMappedResults();
    long count = mongoTemplate.count(new Query(criteria), Product.class);
    return new PageImpl<>(entities, pageable, count);
  }

  private Criteria buildCriteriaSearch(ProductSearchCriteria searchCriteria) {
    var criteria = new Criteria();
    List<Criteria> andFilters = new ArrayList<>();

    // Query by Listed
    if (searchCriteria.isListed()) {
      andFilters.add(Criteria.where(LISTED.getFieldName()).ne(false));
    }

    // Query by Type
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      Criteria typeCriteria = Criteria.where(TYPE.getFieldName()).is(searchCriteria.getType().getCode());
      andFilters.add(typeCriteria);
    }

    // Query by Keyword regex
    if (StringUtils.isNoneBlank(searchCriteria.getKeyword())) {
      Criteria keywordCriteria = createQueryByKeywordRegex(searchCriteria);
      if (keywordCriteria != null) {
        andFilters.add(keywordCriteria);
      }
    }

    if (!CollectionUtils.isEmpty(andFilters)) {
      criteria.andOperator(andFilters);
    }
    return criteria;
  }

  private Criteria createQueryByKeywordRegex(ProductSearchCriteria searchCriteria) {
    List<Criteria> filters = new ArrayList<>();
    var language = searchCriteria.getLanguage();
    if (language == null) {
      language = Language.EN;
    }

    List<DocumentField> filterProperties = new ArrayList<>(ProductSearchCriteria.DEFAULT_SEARCH_FIELDS);
    if (!CollectionUtils.isEmpty(searchCriteria.getFields())) {
      filterProperties.clear();
      filterProperties.addAll(searchCriteria.getFields());
    }
    if (!CollectionUtils.isEmpty(searchCriteria.getExcludeFields())) {
      filterProperties.removeIf(field -> searchCriteria.getExcludeFields().stream()
          .anyMatch(excludeField -> excludeField.name().equals(field.name())));
    }

    for (var property : filterProperties) {
      Criteria filterByKeywordCriteria;
      if (property.isLocalizedSupport()) {
        filterByKeywordCriteria = Criteria.where(
            LOCALIZE_SEARCH_PATTERN.formatted(property.getFieldName(), language.getValue()));
      } else {
        filterByKeywordCriteria = Criteria.where(property.getFieldName());
      }
      var regex = new BsonRegularExpression(searchCriteria.getKeyword(), CASE_INSENSITIVITY_OPTION);
      filters.add(filterByKeywordCriteria.regex(regex));
    }
    Criteria criteria = null;
    if (!CollectionUtils.isEmpty(filters)) {
      criteria = new Criteria().orOperator(filters);
    }
    return criteria;
  }
}
