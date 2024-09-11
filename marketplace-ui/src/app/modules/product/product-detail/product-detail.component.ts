import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  HostListener,
  WritableSignal,
  inject,
  signal
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { MarkdownModule, MarkdownService } from 'ngx-markdown';
import { Observable } from 'rxjs';
import { AuthService } from '../../../auth/auth.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { CommonDropdownComponent } from '../../../shared/components/common-dropdown/common-dropdown.component';
import {
  PRODUCT_DETAIL_TABS,
  VERSION
} from '../../../shared/constants/common.constant';
import { ItemDropdown } from '../../../shared/models/item-dropdown.model';
import { ProductDetail } from '../../../shared/models/product-detail.model';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { HasValueTabPipe } from '../../../shared/pipes/has-value-tab.pipe';
import { MissingReadmeContentPipe } from '../../../shared/pipes/missing-readme-content.pipe';
import { ProductTypeIconPipe } from '../../../shared/pipes/icon.pipe';
import { MultilingualismPipe } from '../../../shared/pipes/multilingualism.pipe';
import { ProductTypePipe } from '../../../shared/pipes/product-type.pipe';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { RoutingQueryParamService } from '../../../shared/services/routing.query.param.service';
import { CommonUtils } from '../../../shared/utils/common.utils';
import { ProductService } from '../product.service';
import { ProductDetailFeedbackComponent } from './product-detail-feedback/product-detail-feedback.component';
import { ProductFeedbackService } from './product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ProductStarRatingService } from './product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { ProductDetailActionType } from '../../../shared/enums/product-detail-action-type';
import { ProductDetailInformationTabComponent } from './product-detail-information-tab/product-detail-information-tab.component';
import { ProductDetailMavenContentComponent } from './product-detail-maven-content/product-detail-maven-content.component';
import { ProductDetailVersionActionComponent } from './product-detail-version-action/product-detail-version-action.component';
import { ProductDetailService } from './product-detail.service';
import { ProductInstallationCountActionComponent } from './product-installation-count-action/product-installation-count-action.component';
import { ProductStarRatingNumberComponent } from './product-star-rating-number/product-star-rating-number.component';

export interface DetailTab {
  activeClass: string;
  tabId: string;
  value: string;
  label: string;
}

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [
    ProductDetailVersionActionComponent,
    CommonModule,
    ProductStarRatingNumberComponent,
    TranslateModule,
    MarkdownModule,
    ProductDetailInformationTabComponent,
    ProductDetailMavenContentComponent,
    NgbNavModule,
    MultilingualismPipe,
    ProductTypePipe,
    ProductDetailFeedbackComponent,
    ProductInstallationCountActionComponent,
    ProductTypeIconPipe,
    HasValueTabPipe,
    MissingReadmeContentPipe,
    CommonDropdownComponent
  ],
  providers: [ProductService, MarkdownService],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent {
  readonly STORAGE_ITEM = 'activeTab';
  readonly DEFAULT_ACTIVE_TAB = 'description';
  themeService = inject(ThemeService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  productService = inject(ProductService);
  languageService = inject(LanguageService);
  productDetailService = inject(ProductDetailService);
  productFeedbackService = inject(ProductFeedbackService);
  productStarRatingService = inject(ProductStarRatingService);
  appModalService = inject(AppModalService);
  authService = inject(AuthService);
  elementRef = inject(ElementRef);
  routingQueryParamService = inject(RoutingQueryParamService);

  resizeObserver: ResizeObserver;

  productDetail: WritableSignal<ProductDetail> = signal({} as ProductDetail);
  productModuleContent: WritableSignal<ProductModuleContent> = signal(
    {} as ProductModuleContent
  );
  productDetailActionType = signal(ProductDetailActionType.STANDARD);
  detailContent!: DetailTab;
  detailTabs = PRODUCT_DETAIL_TABS;
  activeTab = this.DEFAULT_ACTIVE_TAB;
  selectedTabLabel: string = CommonUtils.getLabel(
    PRODUCT_DETAIL_TABS[0].value,
    PRODUCT_DETAIL_TABS
  );
  detailTabsForDropdown = PRODUCT_DETAIL_TABS;
  isDropdownOpen: WritableSignal<boolean> = signal(false);
  isTabDropdownShown: WritableSignal<boolean> = signal(false);
  selectedVersion = '';
  metaProductJsonUrl: string | undefined = '';
  showPopup!: boolean;
  isMobileMode = signal<boolean>(false);
  installationCount = 0;

  @HostListener('window:popstate', ['$event'])
  onPopState() {
    this.activeTab = window.location.hash.split('#tab-')[1];
    if (this.activeTab === undefined) {
      this.activeTab = this.DEFAULT_ACTIVE_TAB;
    }
    this.updateDropdownSelection();
  }

  constructor() {
    this.scrollToTop();
    this.resizeObserver = new ResizeObserver(() => {
      this.updateDropdownSelection();
    });
  }

  ngOnInit(): void {
    const productId = this.route.snapshot.params['id'];
    this.productDetailService.productId.set(productId);
    if (productId) {
      this.getProductById(productId).subscribe(productDetail => {
        this.productDetail.set(productDetail);
        this.productModuleContent.set(productDetail.productModuleContent);
        this.metaProductJsonUrl = productDetail.metaProductJsonUrl;
        this.detailTabsForDropdown = this.getNotEmptyTabs();
        this.productDetailService.productNames.set(productDetail.names);
        localStorage.removeItem(this.STORAGE_ITEM);
        this.installationCount = productDetail.installationCount;
        this.handleProductContentVersion();
        this.updateProductDetailActionType(productDetail);
      });
      this.productFeedbackService.initFeedbacks();
      this.productStarRatingService.fetchData();
    }

    const savedTab = localStorage.getItem(this.STORAGE_ITEM);
    if (savedTab) {
      this.activeTab = savedTab;
    }
    this.updateDropdownSelection();
  }

  handleProductContentVersion() {
    if (this.isEmptyProductContent()) {
      return;
    }
    this.selectedVersion = VERSION.displayPrefix.concat(
      this.convertTagToVersion(this.productModuleContent().tag));
  }

  updateProductDetailActionType(productDetail: ProductDetail) {
    if (productDetail?.sourceUrl === undefined) {
      this.productDetailActionType.set(ProductDetailActionType.CUSTOM_SOLUTION);
    } else if (this.routingQueryParamService.isDesignerEnv()) {
      this.productDetailActionType.set(ProductDetailActionType.DESIGNER_ENV);
    } else {
      this.productDetailActionType.set(ProductDetailActionType.STANDARD)
    }
  }

  scrollToTop() {
    window.scrollTo({ left: 0, top: 0, behavior: 'instant' });
  }

  getProductById(productId: string): Observable<ProductDetail> {
    const targetVersion = this.routingQueryParamService.getDesignerVersionFromCookie();
    if (!targetVersion) {
      return this.productService.getProductDetails(productId);
    }

    return this.productService.getBestMatchProductDetailsWithVersion(
      productId,
      targetVersion
    );
  }

  ngAfterViewInit(): void {
    this.checkMediaSize();
    this.productFeedbackService.findProductFeedbackOfUser().subscribe(() => {
      this.route.queryParams.subscribe(params => {
        this.showPopup = params['showPopup'] === 'true';
        if (this.showPopup && this.authService.getToken()) {
          this.appModalService
            .openAddFeedbackDialog()
            .then(() => this.removeQueryParam())
            .catch(() => this.removeQueryParam());
        }
      });
    });
  }

  isEmptyProductContent(): boolean {
    const content = this.productModuleContent();
    return !content || Object.keys(content).length === 0;
  }

  loadDetailTabs(selectedVersion: string) {
    let version = selectedVersion || this.productDetail().newestReleaseVersion;
    version = version.replace(VERSION.displayPrefix, '');
    this.productService
      .getProductDetailsWithVersion(this.productDetail().id, version)
      .subscribe(updatedProductDetail => {
        this.productModuleContent.set(
          updatedProductDetail.productModuleContent
        );
      });
  }

  onTabChange(event: string) {
    this.setActiveTab(event);
    this.selectedTabLabel = CommonUtils.getLabel(event, PRODUCT_DETAIL_TABS);
    this.isTabDropdownShown.update(value => !value);
    this.onTabDropdownShown();
  }

  updateDropdownSelection() {
    const dropdown = document.getElementById(
      'tab-group-dropdown'
    ) as HTMLSelectElement;
    if (dropdown) {
      dropdown.value = this.activeTab;
    }
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    const hash = '#tab-' + tab;
    const path = window.location.pathname;
    if (history.pushState) {
      history.pushState(null, '', path + hash);
    } else {
      window.location.hash = hash;
    }
    this.updateDropdownSelection();

    localStorage.setItem(this.STORAGE_ITEM, tab);
  }

  onShowInfoContent() {
    this.isDropdownOpen.update(value => !value);
  }

  onTabDropdownShown() {
    this.isTabDropdownShown.set(!this.isTabDropdownShown());
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    const formSelect =
      this.elementRef.nativeElement.querySelector('.form-select');
    if (
      formSelect &&
      !formSelect.contains(event.target) &&
      this.isTabDropdownShown()
    ) {
      this.onTabDropdownShown();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    if (mediaQuery.matches) {
      this.isMobileMode.set(true);
    } else {
      this.isMobileMode.set(false);
    }
  }

  onClickRateBtn() {
    const productId = this.productDetailService.productId();
    if (this.authService.getToken()) {
      this.appModalService.openAddFeedbackDialog();
    } else {
      this.authService.redirectToGitHub(productId);
    }
  }

  receiveInstallationCountData(data: number) {
    this.installationCount = data;
  }

  private removeQueryParam(): void {
    this.router.navigate([], {
      queryParams: { showPopup: null },
      queryParamsHandling: 'merge'
    });
  }

  getNotEmptyTabs(): ItemDropdown[] {
    return this.detailTabsForDropdown.filter(tab =>
      HasValueTabPipe.prototype.transform(
        tab.value,
        this.productModuleContent()
      )
    );
  }

  convertTagToVersion(tag: string): string {
    if (tag !== '' && tag.startsWith(VERSION.tagPrefix)) {
      return tag.substring(1);
    }
    return tag;
  }
}
