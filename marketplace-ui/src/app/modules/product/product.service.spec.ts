import { TestBed } from '@angular/core/testing';

import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Language } from '../../shared/enums/language.enum';
import { SortOption } from '../../shared/enums/sort-option.enum';
import { TypeOption } from '../../shared/enums/type-option.enum';
import {
  MOCK_PRODUCTS,
  MOCK_PRODUCT_DETAIL,
  MOCK_PRODUCT_RELEASES
} from '../../shared/mocks/mock-data';
import { Criteria } from '../../shared/models/criteria.model';
import { VersionData } from '../../shared/models/vesion-artifact.model';
import { ProductService } from './product.service';
import { DEFAULT_PAGEABLE, DEFAULT_PAGEABLE_IN_REST_CLIENT } from '../../shared/constants/common.constant';
import { API_URI } from '../../shared/constants/api.constant';
import { ProductRelease } from '../../shared/models/apis/product-release.mode';

describe('ProductService', () => {
  let products = MOCK_PRODUCTS._embedded.products;
  let service: ProductService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        ProductService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('findProductsByCriteria with should return products properly', () => {
    const searchString = 'Amazon Comprehend';
    const criteria: Criteria = {
      search: searchString,
      sort: SortOption.ALPHABETICALLY,
      type: TypeOption.CONNECTORS,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: false
    };
    service.findProductsByCriteria(criteria).subscribe(response => {
      let products = response._embedded.products;
      for (let i = 0; i < products.length; i++) {
        expect(products[i].type).toEqual(TypeOption.CONNECTORS);
        expect(products[i].names['en'].toLowerCase()).toContain(searchString);
        if (products[i + 1]) {
          expect(
            products[i + 1].names['en'].localeCompare(products[i].names['en'])
          ).toEqual(1);
        }
      }
    });
  });

  it('findProductsByCriteria with empty searchString', () => {
    const criteria: Criteria = {
      search: '',
      sort: null,
      type: null,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: false
    };
    service.findProductsByCriteria(criteria).subscribe(response => {
      expect(response._embedded.products.length).toEqual(products.length);
    });
  });

  it('findProductsByCriteria with popularity order', () => {
    const criteria: Criteria = {
      search: '',
      sort: SortOption.POPULARITY,
      type: null,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: false
    };
    service.findProductsByCriteria(criteria).subscribe(response => {
      let products = response._embedded.products;
      for (let i = 0; i < products.length; i++) {
        if (
          products[i].platformReview &&
          products[i + 1] &&
          products[i + 1].platformReview
        ) {
          expect(Number(products[i + 1].platformReview)).toBeGreaterThanOrEqual(
            Number(products[i].platformReview)
          );
        }
      }
    });
  });

  it('findProductsByCriteria with default sort', () => {
    const criteria: Criteria = {
      search: '',
      sort: SortOption.RECENT,
      type: null,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: false
    };
    service.findProductsByCriteria(criteria).subscribe(response => {
      expect(response._embedded.products.length).toEqual(products.length);
    });
  });

  it('findProductsByCriteria by next page url', () => {
    const criteria: Criteria = {
      nextPageHref:
        'http://localhost:8080/marketplace-service/api/product?type=all&isRESTClient=false&page=1&size=20',
      search: '',
      sort: SortOption.RECENT,
      type: TypeOption.All_TYPES,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: false
    };
    service.findProductsByCriteria(criteria).subscribe(response => {
      expect(response._embedded.products.length).toEqual(0);
      expect(response.page.number).toEqual(1);
    });
  });

  it('findProductsByCriteria should return products with type connectors', () => {
    const searchString = 'Amazon Comprehend';
    const criteria: Criteria = {
      search: '',
      sort: SortOption.ALPHABETICALLY,
      type: null,
      language: Language.EN,
      pageable: DEFAULT_PAGEABLE,
      isRESTClientEditor: true
    };

    service.findProductsByCriteria(criteria).subscribe(response => {
      expect(criteria.pageable).toEqual(DEFAULT_PAGEABLE_IN_REST_CLIENT);
      let products = response._embedded.products;
      for (let i = 0; i < products.length; i++) {
        expect(products[i].type).toEqual(TypeOption.CONNECTORS);
        expect(products[i].names['en'].toLowerCase()).toContain(searchString);
        if (products[i + 1]) {
          expect(
            products[i + 1].names['en'].localeCompare(products[i].names['en'])
          ).toEqual(1);
        }
      }
    });
  });

  it('should call the API and return VersionData[]', () => {
    const mockResponse: VersionData[] = [
      { version: '10.0.1', artifactsByVersion: [] }
    ];

    const productId = 'adobe-acrobat-connector';
    const showDevVersion = true;
    const designerVersion = '10.0.1';

    service
      .sendRequestToProductDetailVersionAPI(
        productId,
        showDevVersion,
        designerVersion
      )
      .subscribe(data => {
        expect(data).toEqual(mockResponse);
      });

    const req = httpMock.expectOne(request => {
      return (
        request.url === `${API_URI.PRODUCT_DETAILS}/${productId}/versions` &&
        request.params.get('designerVersion') === designerVersion &&
        request.params.get('isShowDevVersion') === showDevVersion.toString()
      );
    });

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('getProductDetailsWithVersion should return a product detail', () => {
    const productId = 'jira-connector';
    const version = '10.0.10';

    service.getProductDetailsWithVersion(productId, version).subscribe(data => {
      expect(data).toEqual(MOCK_PRODUCT_DETAIL);
    });

    const req = httpMock.expectOne(request => {
      expect(request.url).toEqual(`${API_URI.PRODUCT_DETAILS}/${productId}/${version}`);

      return true;
    });
  });

  it('sendRequestToUpdateInstallationCount', () => {
    const productId = "google-maps-connector";
    const designerVersion = "10.0.0";

    service.sendRequestToUpdateInstallationCount(productId, designerVersion).subscribe(response => {
      expect(response).toBe(3);
    });

    const req = httpMock.expectOne(`${API_URI.PRODUCT_MARKETPLACE_DATA}/installation-count/${productId}?designerVersion=${designerVersion}`);
    expect(req.request.method).toBe('PUT');
    req.flush(3);
  });

  it('sendRequestToGetProductVersionForDesigner', () => {
    const productId = 'google-maps-connector';

    service.sendRequestToGetProductVersionsForDesigner(productId).subscribe(response => {
      expect(response.length).toBe(3);
      expect(response[0].version).toBe('10.0.2');
      expect(response[1].version).toBe('10.0.1');
      expect(response[2].version).toBe('10.0.0');
    });

    const req = httpMock.expectOne(`${API_URI.PRODUCT_DETAILS}/${productId}/designerversions`);
    expect(req.request.method).toBe('GET');
    req.flush([{ version: '10.0.2' }, { version: '10.0.1' }, { version: '10.0.0' }]);
  });

  it('getLatestArtifactDownloadUrl', () => {
    const productId = 'bpmn-statistic';
    const version = 'dev';
    const artifact = 'bpmn-statistic.zip';

    service.getLatestArtifactDownloadUrl(productId, version, artifact).subscribe(response => {
      expect(response).toBe(
        'https://maven.axonivy.com/com/axonivy/utils/bpmn-statistic/10.0.20/bpmn-statistic.zip'
      );
    });
  });

  it('getProductChangelogs', () => {
    const productId = 'portal';
    const mockResponse: ProductRelease[] = MOCK_PRODUCT_RELEASES;

    service.getProductChangelogs(productId).subscribe(response => {
      expect(response.length).toEqual(mockResponse.length);
    });

    const req = httpMock.expectOne(`${API_URI.PRODUCT_DETAILS}/${productId}/releases`);
    expect(req.request.method).toBe('GET');
    req.flush([{
      name: "12.0.3",
      body: "## Changes\r\n\r\n## 🚀 Features\r\n\r\n- [IVYPORTAL-18158](https://1ivy.atlassian.net/browse/IVYPORTAL-18158) Implement File Preview to Portal Components https://github.com/nhthinh-axonivy (https://github.com/axonivy-market/portal/pull/1443)\r\n",
      publishedAt: "2025-01-20"
    }]);
  });
});
