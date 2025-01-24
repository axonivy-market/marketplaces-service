import { Observable, of } from 'rxjs';
import { Criteria } from '../models/criteria.model';
import { TypeOption } from '../enums/type-option.enum';
import {
  MOCK_PRODUCTS,
  MOCK_PRODUCTS_FILTER_CONNECTOR,
  MOCK_PRODUCTS_NEXT_PAGE,
  MOCK_PRODUCT_DETAIL,
  MOCK_CRON_JOB_PRODUCT_DETAIL,
  MOCK_PRODUCT_RELEASES
} from './mock-data';
import { ProductApiResponse } from '../models/apis/product-response.model';
import { ProductDetail } from '../models/product-detail.model';

export class MockProductService {
  findProductsByCriteria(criteria: Criteria): Observable<ProductApiResponse> {
    let response = MOCK_PRODUCTS;
    if (criteria.nextPageHref) {
      response = MOCK_PRODUCTS_NEXT_PAGE;
    } else if (criteria.type == TypeOption.CONNECTORS) {
      response = MOCK_PRODUCTS_FILTER_CONNECTOR;
    }
    return of(response);
  }

  getProductDetails(productId: string) {
    return of(MOCK_PRODUCT_DETAIL);
  }

  getProductDetailsWithVersion(
    productId: string,
    version: string
  ): Observable<ProductDetail> {
    return of(MOCK_CRON_JOB_PRODUCT_DETAIL);
  }

  getBestMatchProductDetailsWithVersion(
    productId: string,
    version: string
  ): Observable<ProductDetail> {
    return of(MOCK_CRON_JOB_PRODUCT_DETAIL);
  }

  getProductChangelogs(productId: string) {
    return of(MOCK_PRODUCT_RELEASES);
  }
}
