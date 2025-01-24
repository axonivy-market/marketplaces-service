import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from '@angular/core/testing';
import { By, DomSanitizer, Title } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Viewport } from 'karma-viewport/dist/adapter/viewport';
import { of } from 'rxjs';
import { TypeOption } from '../../../shared/enums/type-option.enum';
import {
  MOCK_PRODUCT_DETAIL,
  MOCK_CRON_JOB_PRODUCT_DETAIL,
  MOCK_PRODUCT_MODULE_CONTENT,
  MOCK_PRODUCTS,
  MOCK_FEEDBACK_API_RESPONSE,
} from '../../../shared/mocks/mock-data';
import { ProductService } from '../product.service';
import { ProductDetailComponent } from './product-detail.component';
import { ProductModuleContent } from '../../../shared/models/product-module-content.model';
import { RoutingQueryParamService } from '../../../shared/services/routing.query.param.service';
import { MockProductService } from '../../../shared/mocks/mock-services';
import { ProductDetailActionType } from '../../../shared/enums/product-detail-action-type';
import { LanguageService } from '../../../core/services/language/language.service';
import { Language } from '../../../shared/enums/language.enum';
import { MatomoTestingModule } from 'ngx-matomo-client/testing';
import { AuthService } from '../../../auth/auth.service';
import { AppModalService } from '../../../shared/services/app-modal.service';
import { ProductFeedbackService } from './product-detail-feedback/product-feedbacks-panel/product-feedback.service';
import { ProductStarRatingService } from './product-detail-feedback/product-star-rating-panel/product-star-rating.service';
import { FeedbackApiResponse } from '../../../shared/models/apis/feedback-response.model';
import { StarRatingCounting } from '../../../shared/models/star-rating-counting.model';
import { Feedback } from '../../../shared/models/feedback.model';
import MarkdownIt from 'markdown-it';

const products = MOCK_PRODUCTS._embedded.products;
declare const viewport: Viewport;

describe('ProductDetailComponent', () => {
  let component: ProductDetailComponent;
  let fixture: ComponentFixture<ProductDetailComponent>;
  let routingQueryParamService: jasmine.SpyObj<RoutingQueryParamService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let titleService: Title;
  let sanitizerSpy: jasmine.SpyObj<DomSanitizer>;
  let mockProductFeedbackService: jasmine.SpyObj<ProductFeedbackService>;
  let mockProductStarRatingService: jasmine.SpyObj<ProductStarRatingService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockAppModalService: jasmine.SpyObj<AppModalService>;
  const sourceURL = 'https://github.com/source-repo';

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('DomSanitizer', [
      'bypassSecurityTrustHtml',
      'sanitize'
    ]);
    const routingQueryParamServiceSpy = jasmine.createSpyObj(
      'RoutingQueryParamService',
      ['getDesignerVersionFromSessionStorage', 'isDesignerEnv']
    );

    const languageServiceSpy = jasmine.createSpyObj('LanguageService', [
      'selectedLanguage'
    ]);

    mockProductFeedbackService = jasmine.createSpyObj(
      'ProductFeedbackService',
      [
        'getInitFeedbacksObservable',
        'findProductFeedbacksByCriteria',
        'handleFeedbackApiResponse',
        'findProductFeedbackOfUser',
        'totalElements'
      ]
    );
    mockAuthService = jasmine.createSpyObj('AuthService', ['getToken']);
    mockAppModalService = jasmine.createSpyObj('AppModalService', [
      'openAddFeedbackDialog'
    ]);
    mockProductStarRatingService = jasmine.createSpyObj(
      'ProductStarRatingService',
      ['getRatingObservable', 'starRatings', 'totalComments', 'reviewNumber']
    );

    await TestBed.configureTestingModule({
      imports: [
        ProductDetailComponent,
        TranslateModule.forRoot(),
        MatomoTestingModule.forRoot()
      ],
      providers: [
        ProductStarRatingService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        {
          provide: ProductStarRatingService,
          useValue: mockProductStarRatingService
        },
        {
          provide: ProductFeedbackService,
          useValue: mockProductFeedbackService
        },
        { provide: AuthService, useValue: mockAuthService },
        { provide: AppModalService, useValue: mockAppModalService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { params: { id: products[0].id } },
            queryParams: of({ type: TypeOption.CONNECTORS }),
            fragment: of('description')
          }
        },
        {
          provide: RoutingQueryParamService,
          useValue: routingQueryParamServiceSpy
        },
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        },
        Title,
        { provide: DomSanitizer, useValue: spy }
      ]
    })
      .overrideComponent(ProductDetailComponent, {
        remove: { providers: [ProductService] },
        add: {
          providers: [{ provide: ProductService, useClass: MockProductService }]
        }
      })
      .compileComponents();
    routingQueryParamService = TestBed.inject(
      RoutingQueryParamService
    ) as jasmine.SpyObj<RoutingQueryParamService>;

    languageService = TestBed.inject(
      LanguageService
    ) as jasmine.SpyObj<LanguageService>;

    sanitizerSpy = TestBed.inject(DomSanitizer) as jasmine.SpyObj<DomSanitizer>;

    titleService = TestBed.inject(Title);
    mockProductFeedbackService.getInitFeedbacksObservable.and.returnValue(
      of([MOCK_FEEDBACK_API_RESPONSE] as any as FeedbackApiResponse)
    );

    mockProductFeedbackService.findProductFeedbackOfUser.and.returnValue(
      of({} as any as Feedback)
    );
    mockAppModalService.openAddFeedbackDialog.and.returnValue(
      Promise.resolve()
    );
    mockAuthService.getToken.and.returnValue('token');
    mockProductStarRatingService.getRatingObservable.and.returnValue(
      of([] as any as StarRatingCounting[])
    );
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProductDetailComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component.productDetail().names['en']).toEqual(
      MOCK_PRODUCT_DETAIL.names['en']
    );
  });

  it('should have title like the name DE', () => {
    languageService.selectedLanguage.and.returnValue(Language.DE);
    component.updateWebBrowserTitle();
    fixture.detectChanges();

    expect(titleService.getTitle()).toEqual(
      MOCK_PRODUCT_DETAIL.names[Language.DE]
    );
  });

  it('version should display in number', () => {
    expect(component.selectedVersion).toEqual('Version 10.0.0');
  });

  it('should get corresponding version from session strorage', () => {
    const targetVersion = '1.0';
    const productId = 'Portal';
    routingQueryParamService.getDesignerVersionFromSessionStorage.and.returnValue(
      targetVersion
    );
    component.getProductById(productId, false).subscribe(productDetail => {
      expect(productDetail).toEqual(MOCK_CRON_JOB_PRODUCT_DETAIL);
    });
  });

  it('should reset state before fetching new product details', () => {
    component.productDetail.set(MOCK_PRODUCT_DETAIL);
    component.productModuleContent.set(
      MOCK_PRODUCT_DETAIL.productModuleContent
    );

    expect(component.productDetail().id).toBe('jira-connector');
    expect(component.productModuleContent().name).toBe('Jira Connector');
  });

  it('should update dropdown selection on updateDropdownSelection', () => {
    document.body.innerHTML = `<select id="nav_item"><option value="description">Description</option></select>`;
    component.activeTab = 'description';
    component.updateDropdownSelection();
    const dropdown = document.getElementById('nav_item') as HTMLSelectElement;
    expect(dropdown.value).toBe('description');
  });

  it('should call updateDropdownSelection when setActiveTab is called', () => {
    spyOn(component, 'updateDropdownSelection');
    const tab = 'specifications';
    component.setActiveTab(tab);
    expect(component.updateDropdownSelection).toHaveBeenCalled();
  });

  it('should call setActiveTab and updateDropdownSelection on onTabChange', () => {
    const event = { value: 'description' };
    spyOn(component, 'setActiveTab');
    spyOn(component, 'updateDropdownSelection');

    component.onTabChange(event.value);

    expect(component.setActiveTab).toHaveBeenCalledWith('description');
  });

  it('should not display information when product detail is empty', () => {
    const mockContentWithEmptySetup: ProductModuleContent =
      {} as ProductModuleContent;
    component.productModuleContent.set(mockContentWithEmptySetup);
    expect(component.isEmptyProductContent()).toBeTrue();
    fixture.detectChanges();
    const description = fixture.debugElement.query(By.css('#description'));
    expect(description).toBeFalsy();
  });

  it('should return true for description when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return true for description when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: 'Test description' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeTrue();
  });

  it('should return false for description when it is null', () => {
    const mockContentWithNullDescription: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDescription);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for any tab when detail content is undefined or null', () => {
    component.productModuleContent.set(null as any as ProductModuleContent);
    expect(component.getContent('description')).toBeFalse();
    component.productModuleContent.set(
      undefined as any as ProductModuleContent
    );
    expect(component.getContent('description')).toBeFalse();
    component.productModuleContent.set({} as any as ProductModuleContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { de: 'Test description' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return false for description when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      description: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('description')).toBeFalse();
  });

  it('should return true for setup when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return true for setup when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: 'Test setup' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeTrue();
  });

  it('should return false for setup when it is null', () => {
    const mockContentWithNullSetup: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullSetup);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { de: 'Test setup' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return false for setup when in both DE and EN language it is an undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      setup: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('setup')).toBeFalse();
  });

  it('should return true for demo when in EN language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is not null and not undefined and not empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is empty but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo', de: '' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return true for demo when in DE language it is undefined but in EN language it has value', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: 'Test demo' }
    };

    const selectedLanguage = Language.DE;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeTrue();
  });

  it('should return false for demo when it is null', () => {
    const mockContentWithNullDemo: ProductModuleContent =
      MOCK_PRODUCT_MODULE_CONTENT;
    component.productModuleContent.set(mockContentWithNullDemo);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { de: 'Test demo' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in both DE and EN language it is an empty string', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: { en: '', de: '' }
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for demo when in both DE and EN language it is undefined', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      demo: {}
    };

    const selectedLanguage = Language.EN;

    languageService.selectedLanguage.and.returnValue(selectedLanguage);

    component.productModuleContent.set(mockContent);
    expect(component.getContent('demo')).toBeFalse();
  });

  it('should return false for changelog when productReleaseSafeHtmls is empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
    };

    component.productReleaseSafeHtmls = [];

    component.productModuleContent.set(mockContent);
    expect(component.getContent('changelog')).toBeFalse();
  });

  it('should display dropdown horizontally on small viewport', () => {
    viewport.set(540);
    const tabGroup = fixture.debugElement.query(By.css('.row-tab'));
    tabGroup.triggerEventHandler('click', null);

    fixture.detectChanges();
    const dropdown = fixture.debugElement.query(By.css('.dropdown-tab'));

    expect(getComputedStyle(dropdown.nativeElement).flexDirection).toBe('row');
  });

  it('should display dropdown instead of tabs when viewport width is 540px', () => {
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const tabs = tabGroup.query(By.css('.row-tab d-none d-xl-block col-12'));
    const dropdown = tabGroup.query(By.css('.dropdown-tab'));

    expect(tabs).toBeFalsy();
    expect(dropdown).toBeTruthy();
  });

  it('should display tabs instead of dropdown when viewport width is above 540px', () => {
    viewport.set(1920);
    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const dropdown = tabGroup.query(
      By.css(
        '.dropdown-tab d-block d-xl-none d-flex flex-row justify-content-center align-items-center w-100'
      )
    );

    expect(dropdown).toBeFalsy();
  });

  it('should display info tab on click of info icon for smaller screens', () => {
    viewport.set(540);

    let infoTab = fixture.debugElement.query(
      By.css(
        '.info-tab d-none d-xl-block d-flex flex-column flex-grow-1 align-items-start col-xl-3'
      )
    );
    expect(infoTab).toBeFalsy();

    const infoIcon = fixture.debugElement.query(By.css('.info-icon'));
    infoIcon.triggerEventHandler('click', null);
    fixture.detectChanges();

    infoTab = fixture.debugElement.query(By.css('.info-tab'));
    expect(infoTab).toBeTruthy();
  });

  it('should call checkMediaSize on ngOnInit', fakeAsync(() => {
    spyOn(component, 'checkMediaSize');
    component.ngOnInit();
    tick();
    expect(component.checkMediaSize).toHaveBeenCalled();
  }));

  it('should set isMobileMode based on window size', () => {
    spyOn(window, 'matchMedia').and.returnValue({
      matches: true,
      media: '',
      addEventListener: () => {},
      removeEventListener: () => {},
      onchange: null,
      addListener: function (
        callback:
          | ((this: MediaQueryList, ev: MediaQueryListEvent) => any)
          | null
      ): void {
        throw new Error('Function not implemented.');
      },
      removeListener: function (
        callback:
          | ((this: MediaQueryList, ev: MediaQueryListEvent) => any)
          | null
      ): void {
        throw new Error('Function not implemented.');
      },
      dispatchEvent: function (event: Event): boolean {
        throw new Error('Function not implemented.');
      }
    });

    component.checkMediaSize();
    expect(component.isMobileMode()).toBeTrue();

    (window.matchMedia as jasmine.Spy).and.returnValue({
      matches: false,
      media: '',
      addEventListener: () => {},
      removeEventListener: () => {}
    });

    component.checkMediaSize();
    expect(component.isMobileMode()).toBeFalse();
  });

  it('should call checkMediaSize on window resize', () => {
    spyOn(component, 'checkMediaSize');
    component.onResize();
    expect(component.checkMediaSize).toHaveBeenCalled();
  });

  it('should be empty selected version if product detail content is missing', () => {
    component.productModuleContent.set({} as ProductModuleContent);
    component.selectedVersion = '';
    component.handleProductContentVersion();
    expect(component.selectedVersion).toEqual('');
  });

  it('should be formated selected version if open in designer', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT,
      version: '10.0.11'
    };
    component.productModuleContent.set(mockContent);
    routingQueryParamService.isDesignerEnv.and.returnValue(true);
    component.handleProductContentVersion();
    expect(component.selectedVersion).toEqual('Version 10.0.11');
  });

  it('should return DESIGNER_ENV as acction type in Designer Env', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(true);

    component.updateProductDetailActionType({ sourceUrl: 'some-url' } as any);
    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.DESIGNER_ENV
    );
  });

  it('should return CUSTOM_SOLUTION as acction type when productDetail.sourceUrl is undefined', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);

    component.updateProductDetailActionType({ sourceUrl: undefined } as any);

    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.CUSTOM_SOLUTION
    );
    fixture.detectChanges();
    let installationCount = fixture.debugElement.query(
      By.css('#app-product-installation-count-action')
    );
    expect(installationCount).toBeFalsy();
  });

  it('should return STANDARD as acction type when when productDetail.sourceUrl is defined', () => {
    routingQueryParamService.isDesignerEnv.and.returnValue(false);

    component.updateProductDetailActionType({ sourceUrl: 'some-url' } as any);

    expect(component.productDetailActionType()).toBe(
      ProductDetailActionType.STANDARD
    );
  });

  it('displayed tabs array should have size 0 if product module content description, setup, demo, dependency, and changelog are all empty', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContent);
    component.productReleaseSafeHtmls = [];

    expect(component.displayedTabsSignal().length).toBe(0);
  });

  it('should hide tab and tab content when all tabs have no content', () => {
    const mockContent: ProductModuleContent = {
      ...MOCK_PRODUCT_MODULE_CONTENT
    };
    component.productModuleContent.set(mockContent);

    const tabGroup = fixture.debugElement.query(By.css('.tab-group'));
    const tabs = tabGroup.query(By.css('.row-tab d-none d-xl-block col-12'));
    const dropdown = tabGroup.query(
      By.css(
        '.dropdown-tab d-block d-xl-none d-flex flex-row justify-content-center align-items-center w-100'
      )
    );
    const tabContent = tabGroup.query(
      By.css('.tab-content col-12 default-cursor')
    );

    expect(tabs).toBeFalsy();
    expect(dropdown).toBeFalsy();
    expect(tabContent).toBeFalsy();
  });

  it('should generate right text for the rate connector', () => {
    const rateConnector = fixture.debugElement.query(
      By.css('.rate-connector-btn')
    );
    expect(rateConnector.childNodes[0].nativeNode.textContent).toContain(
      'common.feedback.rateFeedbackForConnectorBtnLabel'
    );

    const rateConnectorEmptyText = fixture.debugElement.query(
      By.css('.rate-empty-text')
    );
    expect(
      rateConnectorEmptyText.childNodes[0].nativeNode.textContent
    ).toContain('common.feedback.noFeedbackForConnectorLabel');

    component.route.snapshot.params['id'] = 'cronjob';
    spyOn(component, 'getProductById').and.returnValue(
      of(MOCK_CRON_JOB_PRODUCT_DETAIL)
    );
    component.ngOnInit();
    fixture.detectChanges();
    expect(rateConnector.childNodes[0].nativeNode.textContent).toContain(
      'common.feedback.rateFeedbackForUtilityBtnLabel'
    );
    expect(
      rateConnectorEmptyText.childNodes[0].nativeNode.textContent
    ).toContain('common.feedback.noFeedbackForUtilityLabel');
  });

  it('maven tab should not display when product module content is missing', () => {
    const event = { value: 'dependency' };
    component.onTabChange(event.value);
    fixture.detectChanges();
    let mavenTab = fixture.debugElement.query(
      By.css('app-product-detail-maven-content')
    );

    expect(mavenTab).toBeTruthy();
    component.productModuleContent.set({} as any as ProductModuleContent);
    fixture.detectChanges();
    mavenTab = fixture.debugElement.query(
      By.css('app-product-detail-maven-content')
    );
    expect(mavenTab).toBeFalsy();
  });

  it('should render GitHub alert as safe HTML', () => {
    const value = '**This is a test**';
    const mockedRenderedHtml = '<strong>This is a test</strong>';
    sanitizerSpy.bypassSecurityTrustHtml.and.returnValue(mockedRenderedHtml);

    const result = component.renderGithubAlert(value);

    expect(result).toBe(mockedRenderedHtml);
  });

  it('should close the dropdown when clicking outside', () => {
    component.isDropdownOpen.set(true);
    fixture.detectChanges();

    const event = new MouseEvent('click');
    document.dispatchEvent(event);
    fixture.detectChanges();

    expect(component.isDropdownOpen()).toBeFalse();
  });

  it('should replace GitHub URLs with appropriate links in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    component.linkifyPullRequests(md, sourceUrl);

    const inputText = 'Check out this PR: https://github.com/source-repo/pull/123';
    const expectedOutput = 'Check out this PR: #123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });

  it('should keep GitHub URLs if they contain compare string in linkifyPullRequests', () => {
    const md = new MarkdownIt();
    const sourceUrl = 'https://github.com/source-repo';
    component.linkifyPullRequests(md, sourceUrl);

    const inputText = 'Check out this PR: https://github.com/source-repo/compare/123';
    const expectedOutput = 'Check out this PR: https://github.com/source-repo/compare/123';
    const result = md.renderInline(inputText);

    expect(result).toContain(expectedOutput);
  });


  it('should render changelog content with safe HTML', () => {
    const mockReleases = [
      {
        name: '1.0.0',
        body: 'Initial release',
        publishedAt: '2023-01-01',
      },
    ];
    const expectedSafeHtml = '<p>Initial release</p>';
    sanitizerSpy.bypassSecurityTrustHtml.and.returnValue(expectedSafeHtml);

    const result = component.renderChangelogContent(mockReleases);

    expect(result[0].body).toBe(expectedSafeHtml);
  });
});