package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(PRODUCT)
public class ProductController {

  private final ProductService service;
  private final ProductModelAssembler assembler;
  private PagedResourcesAssembler<Product> pagedResourcesAssembler;

  public ProductController(ProductService service, ProductModelAssembler assembler,
      PagedResourcesAssembler<Product> pagedResourcesAssembler) {
    this.service = service;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all products by type", description = "Be default system will finds product by type as 'all'")
  @GetMapping("/{type}")
  public ResponseEntity<PagedModel<ProductModel>> fetchAllProducts(@PathVariable(required = false) String type,
      Pageable pageable) {
    var results = service.findProductsByType(type, pageable);
    if (results.isEmpty()) {
      return new ResponseEntity<>(PagedModel.empty(), HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(pagedResourcesAssembler.toModel(results, assembler), HttpStatus.OK);
  }

  @Operation(summary = "Search products by keyword", description = "Be default system will search product by name or description")
  @GetMapping("/search")
  public ResponseEntity<RepresentationModel<?>> searchProducts(@RequestParam(required = false) String keyword,
      Pageable pageable) {
    var results = service.searchProducts(keyword, pageable);
    if (results.isEmpty()) {
      return new ResponseEntity<>(PagedModel.empty(), HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(pagedResourcesAssembler.toModel(results, assembler), HttpStatus.OK);
  }
}
