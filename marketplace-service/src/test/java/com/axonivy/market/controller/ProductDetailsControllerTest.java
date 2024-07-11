package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {
	@Mock
	private ProductService productService;

	@Mock
	VersionService versionService;

	@Mock
	private ProductDetailModelAssembler detailModelAssembler;

	@InjectMocks
	private ProductDetailsController productDetailsController;

	@Test
	void testProductDetails() {
		Mockito.when(productService.fetchProductDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(mockProduct());
		Mockito.when(detailModelAssembler.toModel(Mockito.any())).thenReturn(createProductMockWithDetails());
		ResponseEntity<ProductDetailModel> mockExpectedResult = new ResponseEntity<>(createProductMockWithDetails(),
				HttpStatus.OK);

		ResponseEntity<ProductDetailModel> result = productDetailsController.findProductDetails("docker-connector",
				"connector");

		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(result, mockExpectedResult);

		verify(productService, times(1)).fetchProductDetail("docker-connector", "connector");
		verify(detailModelAssembler, times(1)).toModel(mockProduct());
	}

	@Test
	void testFindProductVersionsById() {
		List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
		Mockito.when(versionService.getArtifactsAndVersionToDisplay(Mockito.anyString(), Mockito.anyBoolean(),
				Mockito.anyString())).thenReturn(models);
		ResponseEntity<List<MavenArtifactVersionModel>> result = productDetailsController
				.findProductVersionsById("protal", true, "10.0.1");
		Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
		Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
		Assertions.assertEquals(models, result.getBody());
	}

	private Product mockProduct() {
		Map<String, String> productName = new HashMap<>();
		productName.put(Language.EN.getValue(), "Docker");
		return Product.builder().id("docker-connector").names(productName).language("English").build();
	}

	private ProductDetailModel createProductMockWithDetails() {
		ProductDetailModel mockProduct = new ProductDetailModel();
		mockProduct.setId("docker-connector");
		Map<String, String> name = new HashMap<>();
		name.put(Language.EN.getValue(), "Docker");
		mockProduct.setNames(name);
		mockProduct.setType("connector");
		mockProduct.setCompatibility("10.0+");
		mockProduct.setSourceUrl("https://github.com/axonivy-market/docker-connector");
		mockProduct.setStatusBadgeUrl("https://github.com/axonivy-market/docker-connector");
		mockProduct.setLanguage("English");
		mockProduct.setIndustry("Cross-Industry");
		mockProduct.setContactUs(false);
		return mockProduct;
	}
}
