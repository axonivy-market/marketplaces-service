package com.axonivy.market.service.impl;

import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceImplTest {

  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepository;

  @Mock
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @Mock
  RestTemplate restTemplate;

  @InjectMocks
  FileDownloadServiceImpl fileDownloadService;

  @Test
  void testDownloadAndUnzipFileWithEmptyResult() throws IOException {
    var result = fileDownloadService.downloadAndUnzipFile("", false);
    assertTrue(result.isEmpty());

    result = fileDownloadService.downloadAndUnzipFile(DOWNLOAD_URL, false);
    assertTrue(result.isEmpty());
  }

  @Test
  void testDownloadAndUnzipFile() {
    assertThrows(ResourceAccessException.class, () -> fileDownloadService.downloadAndUnzipFile(DOWNLOAD_URL, true));
  }

}
