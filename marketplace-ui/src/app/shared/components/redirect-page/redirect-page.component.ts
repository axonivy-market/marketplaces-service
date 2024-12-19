import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ROUTER } from '../../constants/router.constant';
import { TranslateModule } from '@ngx-translate/core';
import { ERROR_PAGE_PATH } from '../../constants/common.constant';
import { ProductService } from '../../../modules/product/product.service';
import { API_URI } from '../../constants/api.constant';
import { ExternalDocument } from '../../models/external-document.model';

const INDEX_FILE = '/index.html';

@Component({
  selector: 'redirect-page',
  standalone: true,
  imports: [TranslateModule],
  template: "<p>{{ 'common.labels.redirecting' | translate }}</p>",
  providers: [ProductService]
})
export class RedirectPageComponent implements OnInit {
  httpClient = inject(HttpClient);
  productService = inject(ProductService);

  constructor(
    private readonly activeRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const product = this.activeRoute.snapshot.paramMap.get(ROUTER.ID);
    const version = this.activeRoute.snapshot.paramMap.get(ROUTER.VERSION);
    const currentUrl = window.location.href;
    const artifact = this.activeRoute.snapshot.paramMap.get(ROUTER.ARTIFACT);

    if (product && version) {
      if (artifact) {
        this.fetchLatestLibVersionDownloadUrl(product, version, artifact);
        return;
      }
      // this.fetchDocumentUrl(product, version, currentUrl);
    }
  }

  fetchDocumentUrl(product: string, version: string, currentUrl: string): void {
    this.httpClient.get<ExternalDocument>(`${API_URI.EXTERNAL_DOCUMENT}/${product}/${version}`)
      .subscribe({
        next: (response: ExternalDocument) => this.handleRedirection(response, currentUrl)
    });
  }

  fetchLatestLibVersionDownloadUrl( product: string, version: string, artifact: string): void {
    this.productService
      .getLatestArtifactDownloadUrl(product, version, artifact)
      .subscribe(downloadUrl => {
        window.location.href = downloadUrl;
      });
  }

  handleRedirection(response: ExternalDocument, currentUrl: string): void {
    if (response === null || response.relativeLink === '') {
      this.router.navigate([ERROR_PAGE_PATH]);
    }
    const relativeUrl = response.relativeLink;
    const isSameUrl = currentUrl === relativeUrl || currentUrl + INDEX_FILE === relativeUrl;
    if (!isSameUrl) {
      window.location.href = relativeUrl;
    }
  }
}
