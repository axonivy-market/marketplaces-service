import { TypeOption } from '../enums/type-option.enum';
import { FeedbackSortType } from '../enums/feedback-sort-type';
import { Language } from '../enums/language.enum';
import { SortOption } from '../enums/sort-option.enum';
import { NavItem } from '../models/nav-item.model';
import { Pageable } from '../models/apis/pageable.model';
import { ItemDropdown } from '../models/item-dropdown.model';

export const NAV_ITEMS: NavItem[] = [
  {
    label: 'common.nav.news',
    link: 'https://developer.axonivy.com/news'
  },
  {
    label: 'common.nav.doc',
    link: 'https://developer.axonivy.com/doc'
  },
  {
    label: 'common.nav.tutorial',
    link: 'https://developer.axonivy.com/tutorial'
  },
  {
    label: 'common.nav.community',
    link: 'https://community.axonivy.com/'
  },
  {
    label: 'common.nav.team',
    link: 'https://developer.axonivy.com/team'
  },
  {
    label: 'common.nav.market',
    link: '/'
  }
];

export const SOCIAL_MEDIA_LINK = [
  {
    styleClass: 'fa-brands fa-linkedin-in',
    title: 'Axon Ivy | LinkedIn',
    url: 'https://www.linkedin.com/company/axon-ivy-ag'
  },
  {
    styleClass: 'fa-brands fa-xing',
    title: 'Axon Ivy | XING',
    url: 'https://www.xing.com/companies/axonivyag'
  },
  {
    styleClass: 'fa-brands fa-facebook-f',
    title: 'Axon Ivy | Facebook',
    url: 'https://www.facebook.com/axonivy'
  },
  {
    styleClass: 'fa-brands fa-youtube',
    title: 'Axon Ivy | Youtube',
    url: 'https://www.youtube.com/channel/UCkoNcDoeDAVM7FB-txy3jnQ'
  },
];

export const IVY_FOOTER_LINKS = [
  {
    containerStyleClass: 'w-md-100 footer__ivy-tag',
    label: 'common.footer.ivyCompanyInfo',
    link: ''
  },
  {
    containerStyleClass: 'footer__ivy-policy-tag',
    label: 'common.footer.privacyPolicy',
    link: 'https://www.axonivy.com/privacy-policy'
  },
];

export const LANGUAGES = [
  {
    value: Language.DE,
    label: 'DE'
  },
  {
    value: Language.EN,
    label: 'EN'
  }
];

export const RATING_LABELS_BY_TYPE = [
  {
    type: 'connector',
    btnLabel: 'common.feedback.rateFeedbackForConnectorBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForConnectorLabel'
  },
  {
    type: 'solution',
    btnLabel: 'common.feedback.rateFeedbackForSolutionBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForSolutionLabel'
  },
  {
    type: 'util',
    btnLabel: 'common.feedback.rateFeedbackForUtilityBtnLabel',
    noFeedbackLabel: 'common.feedback.noFeedbackForUtilityLabel'
  }
];

export const FILTER_TYPES: ItemDropdown<TypeOption>[] = [
  {
    value: TypeOption.All_TYPES,
    label: 'common.filter.value.allTypes'
  },
  {
    value: TypeOption.CONNECTORS,
    label: 'common.filter.value.connector'
  },
  {
    value: TypeOption.UTILITIES,
    label: 'common.filter.value.util'
  },
  {
    value: TypeOption.SOLUTION,
    label: 'common.filter.value.solution'
  }
];

export const SORT_TYPES: ItemDropdown<SortOption>[] = [
  {
    value: SortOption.STANDARD,
    label: 'common.sort.value.standard'
  },
  {
    value: SortOption.POPULARITY,
    label: 'common.sort.value.popularity'
  },
  {
    value: SortOption.ALPHABETICALLY,
    label: 'common.sort.value.alphabetically'
  },
  {
    value: SortOption.RECENT,
    label: 'common.sort.value.recent'
  }
];

export const PRODUCT_DETAIL_TABS: ItemDropdown[] = [
  {
    activeClass: "activeTab === 'description'",
    tabId: 'description-tab',
    value: 'description',
    label: 'common.product.detail.description'
  },
  {
    activeClass: "activeTab === 'demo'",
    tabId: 'demo-tab',
    value: 'demo',
    label: 'common.product.detail.demo'
  },
  {
    activeClass: "activeTab === 'setup'",
    tabId: 'setup-tab',
    value: 'setup',
    label: 'common.product.detail.installationGuide'
  },
  {
    activeClass: "activeTab === 'dependency'",
    tabId: 'dependency-tab',
    value: 'dependency',
    label: 'common.product.detail.maven.label'
  },
  {
    activeClass: "activeTab === 'changelog'",
    tabId: 'changelog-tab',
    value: 'changelog',
    label: 'common.product.detail.changelog'
  }
];

export const FEEDBACK_SORT_TYPES: ItemDropdown<FeedbackSortType>[] = [
  {
    value: FeedbackSortType.NEWEST,
    label: 'common.sort.value.newest'
  },
  {
    value: FeedbackSortType.OLDEST,
    label: 'common.sort.value.oldest'
  },
  {
    value: FeedbackSortType.HIGHEST,
    label: 'common.sort.value.highest'
  },
  {
    value: FeedbackSortType.LOWEST,
    label: 'common.sort.value.lowest'
  }
];

export const DESIGNER_SESSION_STORAGE_VARIABLE = {
  ivyViewerParamName: 'ivy-viewer',
  ivyVersionParamName: 'ivy-version',
  defaultDesignerViewer: 'designer-market',
  restClientParamName: 'resultsOnly',
  searchParamName: 'search'
};

export const DEFAULT_PAGEABLE: Pageable = {
  page: 0,
  size: 20
};
export const DEFAULT_PAGEABLE_IN_REST_CLIENT: Pageable = {
  page: 0,
  size: 40
};

export const VERSION = {
  displayPrefix: 'Version '
};

export const I18N_ERROR_CODE_PATH = 'common.error.description';
export const I18N_DEFAULT_ERROR_CODE = 'default';

export const ERROR_PAGE = 'Error Page';
export const ERROR_PAGE_PATH = 'error-page';
export const NOT_FOUND_ERROR_CODE = 404;
export const INTERNAL_SERVER_ERROR_CODE = 500;
export const USER_NOT_FOUND_ERROR_CODE = 2103;
export const UNDEFINED_ERROR_CODE = 0;
export const BAD_REQUEST_ERROR_CODE = 400;
export const UNAUTHORIZED = 401;
export const FORBIDDEN = 403;
export const REQUEST_TIMEOUT = 408;
export const BAD_GATEWAY = 502;
export const SERVICE_UNAVAILABLE = 503;
export const GATEWAY_TIMEOUT = 504;
export const ERROR_CODES = [
  UNDEFINED_ERROR_CODE,
  NOT_FOUND_ERROR_CODE,
  INTERNAL_SERVER_ERROR_CODE,
  BAD_REQUEST_ERROR_CODE,
  UNAUTHORIZED,
  FORBIDDEN,
  REQUEST_TIMEOUT,
  BAD_GATEWAY,
  SERVICE_UNAVAILABLE,
  GATEWAY_TIMEOUT
];
export const TOKEN_KEY = 'token';

export const DEFAULT_IMAGE_URL = '/assets/images/misc/axonivy-logo-round.png';
export const DOWNLOAD_URL = 'https://developer.axonivy.com/download';
export const SEARCH_URL = 'https://developer.axonivy.com/search';
export const GITHUB_MARKET_ORG_URL = 'https://github.com/axonivy-market';
export const SHOW_DEV_VERSION = "showDevVersions";
export const DEFAULT_VENDOR_IMAGE = '/assets/images/misc/axonivy-logo.svg';
export const DEFAULT_VENDOR_IMAGE_BLACK = '/assets/images/misc/axonivy-logo-black.svg';

export const SECONDS_IN_A_MINUTE = 60;
export const MINUTES_IN_A_HOUR = 60;
export const HOURS_IN_A_DAY = 24;
export const DAYS_IN_A_WEEK = 7;
export const DAYS_IN_A_MONTH = 30;
export const DAYS_IN_A_YEAR = 365;

export const MAX_FEEDBACK_LENGTH =250;

export const SECURITY_MONITOR_SESSION_KEYS = {
  DATA: 'security-monitor-data',
  TOKEN: 'security-monitor-token',
};

export const SECURITY_MONITOR_MESSAGES = {
  TOKEN_REQUIRED: 'Token is required',
  UNAUTHORIZED_ACCESS: 'Unauthorized access.',
  FETCH_FAILURE: 'Failed to fetch security data. Check logs for details.',
};

export const TIME_UNITS = [
  { SECONDS: 60, SINGULAR: 'minute', PLURAL: 'minutes' },
  { SECONDS: 3600, SINGULAR: 'hour', PLURAL: 'hours' },
  { SECONDS: 86400, SINGULAR: 'day', PLURAL: 'days' },
  { SECONDS: 604800, SINGULAR: 'week', PLURAL: 'weeks' },
  { SECONDS: 2592000, SINGULAR: 'month', PLURAL: 'months' },
  { SECONDS: 31536000, SINGULAR: 'year', PLURAL: 'years' },
];

export const REPO_PAGE_PATHS: Record<string, string> = {
  security: '/security',
  dependabot: '/security/dependabot',
  codeScanning: '/security/code-scanning',
  secretScanning: '/security/secret-scanning',
  branches: '/settings/branches',
  lastCommit: '/commit/',
};
