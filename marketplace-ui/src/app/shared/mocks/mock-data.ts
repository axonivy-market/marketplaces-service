import { FeedbackApiResponse } from '../models/apis/feedback-response.model';
import { ProductRelease } from '../models/apis/product-release.mode';
import { ProductApiResponse } from '../models/apis/product-response.model';
import { ExternalDocument } from '../models/external-document.model';
import { ProductDetail } from '../models/product-detail.model';
import { ProductModuleContent } from '../models/product-module-content.model';
import { ReleasePreviewData } from '../models/release-preview-data.model';
import { StarRatingCounting } from '../models/star-rating-counting.model';

export const MOCK_PRODUCTS = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: 'TODO Amazon Comprehend'
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: 'Amazon Comprehend ist ein KI-Service, der maschinelles Lernen nutzt, um aus unstrukturierten Daten wertvolle Informationen zu generieren.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend/logo.png',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_EMPTY_DE_VALUES_AND_NO_LOGO_URL_PRODUCTS = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: ''
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: ''
        },
        logoUrl: '',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_PRODUCTS_FILTER_CONNECTOR = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: {
          en: 'Amazon Comprehend',
          de: 'TODO Amazon Comprehend'
        },
        shortDescriptions: {
          en: 'Amazon Comprehend is a AI service that uses machine learning to uncover information in unstructured data.',
          de: 'Amazon Comprehend ist ein KI-Service, der maschinelles Lernen nutzt, um aus unstrukturierten Daten wertvolle Informationen zu generieren.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/amazon-comprehend/logo.png',
        type: 'connector',
        tags: ['AI'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/amazon-comprehend?type=connector'
          }
        }
      },
      {
        id: 'a-trust',
        names: {
          en: 'A-Trust',
          de: 'A-Trust'
        },
        shortDescriptions: {
          en: 'Clearly authenticate your Austrian customers with a mobile phone signature.',
          de: 'Clearly authenticate your Austrian customers with a mobile phone signature.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/a-trust/logo.png',
        type: 'connector',
        tags: ['e-signature'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/a-trust?type=connector'
          }
        }
      },
      {
        id: 'mailstore-connector',
        names: {
          en: 'Mailstore',
          de: 'Mailstore'
        },
        shortDescriptions: {
          en: 'Enhance business processes by streamlining email management, supporting both IMAP and POP3 with robust SSL encryption.',
          de: 'Enhance business processes by streamlining email management, supporting both IMAP and POP3 with robust SSL encryption.'
        },
        logoUrl:
          'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/mailstore-connector/logo.png',
        type: 'connector',
        tags: ['office', 'email'],
        _links: {
          self: {
            href: 'http://localhost:8080/marketplace-service/api/product-details/mailstore-connector?type=connector'
          }
        }
      }
    ]
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    next: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    },
    last: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=3&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 70,
    totalPages: 4,
    number: 0
  }
} as unknown as ProductApiResponse;

export const MOCK_PRODUCTS_NEXT_PAGE = {
  _embedded: {
    products: []
  },
  _links: {
    first: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=0&size=20'
    },
    self: {
      href: 'http://localhost:8080/marketplace-service/api/product?type=all&page=1&size=20'
    }
  },
  page: {
    size: 20,
    totalElements: 1,
    totalPages: 1,
    number: 1
  }
} as ProductApiResponse;

export const MOCK_PRODUCT_MODULE_CONTENT: ProductModuleContent = {
  version: '10.0.10',
  description: null,
  demo: null,
  setup: null,
  isDependency: false,
  name: 'Jira Connector',
  groupId: 'com.axonivy.connector.jira',
  artifactId: 'jira-connector',
  type: 'iar',
  productId: 'jira-connector'
};

export const MOCK_CRON_JOB_PRODUCT_DETAIL: ProductDetail = {
  id: 'cronjob',
  names: {
    de: 'Cron Job',
    en: 'Cron Job'
  },
  shortDescriptions: {
    de: 'Das Cron-Job-Utility übernimmt die automatische Verwaltung deiner zeitgesteuerten Aufgaben.',
    en: 'Cron Job Utility handles your scheduled jobs autonomously.'
  },
  logoUrl:
    'https://raw.githubusercontent.com/axonivy-market/market/feature/MARP-463-Multilingualism-for-Website/market/utils/cronjob/logo.png',
  type: 'util',
  tags: ['utils'],
  vendor: 'Axon Ivy AG',
  platformReview: '4.5',
  newestReleaseVersion: 'v10.0.4',
  cost: 'Free',
  sourceUrl: 'https://github.com/axonivy-market/cronjob',
  statusBadgeUrl:
    'https://github.com/axonivy-market/cronjob/actions/workflows/ci.yml/badge.svg',
  language: 'English',
  industry: 'Cross-Industry',
  compatibility: '10.0+',
  contactUs: false,
  vendorUrl: '',
  productModuleContent: {
    version: '10.0.4',
    description: {
      en: '**Cron Job** is a job-firing schedule that recurs based on calendar-like notions.\n\nThe [Quartz framework](http://www.quartz-scheduler.org/) is used as underlying scheduler framework.\n\nWith Cron Job, you can specify firing-schedules such as “every Friday at noon”, or “every weekday and 9:30 am”, or even “every 5 minutes between 9:00 am and 10:00 am on every Monday, Wednesday and Friday during January”.\n\nFor more details about Cron Expressions please refer to [Lesson 6: CronTrigger](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-06.html)'
    },
    setup: {
      en: 'No special setup is needed for this demo. Only start the Engine and watch out the logging which will be updated every 5 seconds with the following logging entry:\n\n```\n\nCron Job ist started at: 2023-01-27 10:43:20.\n\n```'
    },
    demo: {
      en: 'In this demo, the CronByGlobalVariableTriggerStartEventBean is defined as the Java class to be executed in the Ivy Program Start element.\n\n![Program Start Element screenshot](https://raw.githubusercontent.com/axonivy-market/cronjob/v10.0.4/cronjob-product/ProgramStartElement.png)\n\nThis bean gets a cron expression via the variable defined as Cron expression and it will schedule by using the expression.\n\n![custom editor UI screenshot](https://raw.githubusercontent.com/axonivy-market/cronjob/v10.0.4/cronjob-product/customEditorUI.png)\n\nFor this demo, the Cron expression is defining the time to start the cron that simply fires every 5 seconds.\n\n```\n\n  demoStartCronPattern: 0/5 * * * * ?\n\n```'
    },
    isDependency: true,
    name: 'cron job',
    groupId: 'com.axonivy.utils.cronjob',
    artifactId: 'cronjob',
    type: 'iar',
    productId: 'cronjob'
  },
  installationCount: 0,
  mavenDropins: false,
  _links: {
    self: {
      href: 'http://localhost:8080/api/product-details/cronjob'
    }
  },
  vendorImage: '/assets/images/misc/axonivy-logo-black.svg',
  vendorImageDarkMode: '/assets/images/misc/axonivy-logo.svg'
};

export const MOCK_PRODUCT_DETAIL: ProductDetail = {
  id: 'jira-connector',
  names: {
    en: 'Atlassian Jira',
    de: 'TODO Atlassian Jira'
  },
  shortDescriptions: {
    en: "Atlassian's Jira connector lets you track issues directly from the Axon Ivy platform.",
    de: "TODO Atlassian's Jira connector lets you track issues directly from the Axon Ivy platform."
  },
  installationCount: 1,
  logoUrl:
    'https://raw.githubusercontent.com/axonivy-market/market/master/market/connector/jira/logo.png',
  type: 'connector',
  tags: ['helper'],
  vendor: 'FROX AG',
  vendorUrl: 'https://www.frox.ch',
  platformReview: '4.5',
  newestReleaseVersion: 'v10.0.0',
  cost: 'Free',
  sourceUrl: 'https://github.com/axonivy-market/jira-connector',
  statusBadgeUrl:
    'https://github.com/axonivy-market/jira-connector/actions/workflows/ci.yml/badge.svg',
  language: 'English',
  industry: 'Cross-Industry',
  compatibility: '9.2+',
  contactUs: false,
  productModuleContent: {
    version: '10.0.0',
    description: {
      en: "Axon Ivy's [Atlassian Jira Connector ](https://www.atlassian.com/software/jira) gives you full power to track issues within your process work. The connector:\n\n- Features three main functionalities (create comment, create issue, and get issue).\n- Provides access to the core API of Atlassian Jira.\n- Supports you with an easy-to-copy demo implementation to reduce your integration effort.\n- Enables low code citizen developers to integrate issue tracking tools without writing a single line of code."
    },
    setup: {
      en: 'Open the `Config/variables.yaml` in your Axon Ivy Designer and paste the\ncode below and adjust the values to your environment.\n\n```\nVariables:\n\n  jira-connector:\n  \n    # Url to the Jira server\n    Url: "https://localhost"\n\n    # Username to connect to the Jira server\n    Username: "admin"\n\n    # Password to connect to the Jira server\n    Password: "1234"\n```'
    },
    demo: {
      en: '![jira-connector Demo 1](https://raw.githubusercontent.com/axonivy-market/jira-connector/v10.0.0/jira-connector-product/images/create-issue.png "Create Jira issue")\n![jira-connector Demo 2](https://raw.githubusercontent.com/axonivy-market/jira-connector/v10.0.0/jira-connector-product/images/create-comment.png "Craete Jira comment")'
    },
    isDependency: true,
    name: 'Jira Connector',
    groupId: 'com.axonivy.connector.jira',
    artifactId: 'jira-connector',
    type: 'iar',
    productId: 'jira-connector'
  },
  mavenDropins: false,
  _links: {
    self: {
      href: 'http://localhost:8082/api/product-details/jira-connector?type=connector'
    }
  },
  vendorImage: '/assets/images/misc/axonivy-logo-black.svg',
  vendorImageDarkMode: '/assets/images/misc/axonivy-logo.svg'
};

export const MOCK_EXTERNAL_DOCUMENT: ExternalDocument = {
  productId: 'portal',
  version: 'v10.0.0',
  artifactId: 'portal-guide',
  artifactName: 'Portal Guide',
  relativeLink: '/market-cache/portal/portal-guide/10.0.0/doc/index.html'
};

export const MOCK_RELEASE_PREVIEW_DATA: ReleasePreviewData = {
  description: {
    English: 'This is a description in English.',
    Spanish: 'Esta es una descripción en español.',
    French: 'Ceci est une description en français.'
  },
  setup: {
    English: 'To set up the application, follow these steps...',
    Spanish: 'Para configurar la aplicación, siga estos pasos...',
    French: "Pour configurer l'application, suivez ces étapes..."
  },
  demo: {
    English: 'To demo the app, use the following commands...',
    Spanish: 'Para mostrar la aplicación, use los siguientes comandos...',
    French: "Pour démontrer l'application, utilisez les commandes suivantes..."
  }
};

export const MOCK_FEEDBACK_API_RESPONSE: FeedbackApiResponse = {
  _embedded: {
    feedbacks: [
      {
        content: 'cool stuff',
        rating: 5,
        productId: 'portal'
      }
    ]
  },
  _links: {
    self: { href: '/feedbacks' }
  },
  page: {
    size: 10,
    totalElements: 1,
    totalPages: 1,
    number: 0
  }
};

export const MOCK_PRODUCT_RELEASES: ProductRelease[] = [
  {
    "name": "12.0.3",
    "body": "## Changes\r\n\r\n## 🚀 Features\r\n\r\n- [IVYPORTAL-18158](https://1ivy.atlassian.net/browse/IVYPORTAL-18158) Implement File Preview to Portal Components https://github.com/nhthinh-axonivy (https://github.com/axonivy-market/portal/pull/1443)\r\n",
    "publishedAt": "2025-01-20"
  }
];