import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  OnDestroy,
  signal,
  ViewChild,
  WritableSignal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { debounceTime, Subject, Subscription } from 'rxjs';
import { ThemeService } from '../../core/services/theme/theme.service';
import { TypeOption } from '../../shared/enums/type-option.enum';
import { SortOption } from '../../shared/enums/sort-option.enum';
import { Criteria } from '../../shared/models/criteria.model';
import { Product } from '../../shared/models/product.model';
import { ProductCardComponent } from './product-card/product-card.component';
import { ProductFilterComponent } from './product-filter/product-filter.component';
import { ProductService } from './product.service';
import { ProductApiResponse } from '../../shared/models/apis/product-response.model';
import { Link } from '../../shared/models/apis/link.model';
import { Page } from '../../shared/models/apis/page.model';
import { Language } from '../../shared/enums/language.enum';
import { ProductDetail } from '../../shared/models/product-detail.model';
import { LanguageService } from '../../core/services/language/language.service';
import { RoutingQueryParamService } from '../../shared/services/routing.query.param.service';
import {
  DEFAULT_PAGEABLE,
  DEFAULT_PAGEABLE_IN_REST_CLIENT,
  DESIGNER_SESSION_STORAGE_VARIABLE
} from '../../shared/constants/common.constant';
import { ItemDropdown } from '../../shared/models/item-dropdown.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { LoadingService } from '../../core/services/loading/loading.service';
import { LoadingComponentId } from '../../shared/enums/loading-component-id';

const SEARCH_DEBOUNCE_TIME = 500;

@Component({
  selector: 'app-product',
  standalone: true,
  imports: [
    LoadingSpinnerComponent,
    CommonModule,
    FormsModule,
    TranslateModule,
    ProductCardComponent,
    ProductFilterComponent
  ],
  providers: [ProductService],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements AfterViewInit, OnDestroy {
  protected LoadingComponentId = LoadingComponentId;
  products: WritableSignal<Product[]> = signal([]);
  productDetail!: ProductDetail;
  subscriptions: Subscription[] = [];
  searchTextChanged = new Subject<string>();
  loadingService = inject(LoadingService);
  criteria: Criteria = {
    search: '',
    type: TypeOption.All_TYPES,
    isRESTClientEditor: false,
    sort: SortOption.STANDARD,
    language: Language.EN,
    pageable: DEFAULT_PAGEABLE
  };
  responseLink!: Link;
  responsePage!: Page;
  isRESTClient: WritableSignal<boolean> = signal(false);
  isDesignerEnvironment = inject(RoutingQueryParamService).isDesignerEnv();

  productService = inject(ProductService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  languageService = inject(LanguageService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  @ViewChild('observer', { static: true }) observerElement!: ElementRef;

  constructor() {
    this.route.queryParams.subscribe(params => {
      this.isRESTClient.set(
        DESIGNER_SESSION_STORAGE_VARIABLE.restClientParamName in params &&
        this.isDesignerEnvironment
      );

      if (params[DESIGNER_SESSION_STORAGE_VARIABLE.searchParamName] != null) {
        this.criteria.search =
          params[DESIGNER_SESSION_STORAGE_VARIABLE.searchParamName];
      }
    });

    this.loadProductItems();
    this.subscriptions.push(
      this.searchTextChanged
        .pipe(debounceTime(SEARCH_DEBOUNCE_TIME))
        .subscribe(value => {
          this.criteria = {
            ...this.criteria,
            nextPageHref: '',
            search: value
          };
          this.loadProductItems(true);
        })
    );
    this.router.events?.subscribe(event => {
      if (!(event instanceof NavigationStart)) {
        return;
      }
      window.scrollTo(0, 0);
    });
  }

  ngAfterViewInit(): void {
    this.setupIntersectionObserver();
  }

  viewProductDetail(productId: string, _productTag: string) {
    if (this.isRESTClient()) {
      window.location.href = `/${productId}`;
    }
    this.router.navigate([`/${productId}`]);
  }

  onFilterChange(selectedType: ItemDropdown<TypeOption>) {
    this.criteria = {
      ...this.criteria,
      nextPageHref: '',
      type: selectedType.value
    };
    this.loadProductItems(true);
    const queryParams = selectedType.value !== TypeOption.All_TYPES
      ? { type: this.criteria.type }
      : { type: null };

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: queryParams,
      queryParamsHandling: 'merge'
    });
  }

  onSortChange(selectedSort: SortOption) {
    this.criteria = {
      ...this.criteria,
      nextPageHref: '',
      sort: selectedSort
    };
    this.loadProductItems(true);
    let queryParams = null;
    if (selectedSort !== SortOption.STANDARD) {
      queryParams = { sort: this.criteria.sort };
    } else {
      queryParams = { sort: null };
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParamsHandling: 'merge',
      queryParams
    });
  }

  onSearchChanged(searchString: string) {
    this.criteria = {
      ...this.criteria,
      search: searchString
    };
    this.searchTextChanged.next(searchString);

    const queryParams = searchString
      ? { search: this.criteria.search }
      : { search: null };

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: queryParams,
      queryParamsHandling: 'merge'
    });
  }

  loadProductItems(shouldCleanData = false) {
    this.criteria.language = this.languageService.selectedLanguage();
    if (this.isRESTClient()) {
      this.criteria = {
        ...this.criteria,
        isRESTClientEditor: true,
        type: TypeOption.CONNECTORS,
        language: Language.EN,
        pageable: DEFAULT_PAGEABLE_IN_REST_CLIENT
      };
    }

    this.subscriptions.push(
      this.productService
        .findProductsByCriteria(this.criteria)
        .subscribe((response: ProductApiResponse) => {
          const newProducts = response._embedded.products;
          if (shouldCleanData) {
            this.products.set(newProducts);
          } else {
            this.products.update(existingProducts =>
              existingProducts.concat(newProducts)
            );
          }
          this.responseLink = response._links;
          this.responsePage = response.page;
        })
    );
  }

  setupIntersectionObserver() {
    const options = { root: null, rootMargin: '10px', threshold: 0.1 };
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting && this.hasMore()) {
          this.criteria.nextPageHref = this.responseLink?.next?.href;
          this.loadProductItems();
        }
      });
    }, options);

    observer.observe(this.observerElement.nativeElement);
  }

  hasMore() {
    if (!this.responsePage || !this.responseLink) {
      return false;
    }
    return (
      this.responsePage.number < this.responsePage.totalPages &&
      this.responseLink?.next !== undefined
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => {
      sub.unsubscribe();
    });
  }
}
