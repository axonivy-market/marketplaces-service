import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../../environments/environment';
import { ProductSecurityInfo } from '../../../shared/models/product-security-info-model';
import { SecurityMonitorService } from '../../security-monitor/security-monitor.service';
import { SecurityMonitorComponent } from '../../security-monitor/security-monitor.component';

describe('SecurityMonitorService', () => {
  let service: SecurityMonitorService;
  let httpMock: HttpTestingController;

  const mockApiUrl = environment.apiUrl + '/api/security-monitor';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SecurityMonitorComponent],
      providers: [
        SecurityMonitorService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(SecurityMonitorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call API with token and return security details', () => {
    const mockToken = 'valid-token';
    const mockResponse: ProductSecurityInfo[] = [
      {
        repoName: 'repo1',
        visibility: 'public',
        archived: false,
        dependabot: { status: 'ENABLED', alerts: {} },
        codeScanning: { status: 'ENABLED', alerts: {} },
        secretScanning: { status: 'ENABLED', numberOfAlerts: 0 },
        branchProtectionEnabled: true,
        lastCommitSHA: '12345',
        lastCommitDate: '',
      },
    ];

    service.getSecurityDetails(mockToken).subscribe((data) => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(mockApiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

    req.flush(mockResponse);
  });

  it('should handle error response gracefully', () => {
    const mockToken = 'invalid-token';

    service.getSecurityDetails(mockToken).subscribe({
      next: () => fail('Expected an error, but received data.'),
      error: (error) => {
        expect(error.status).toBe(401);
      },
    });

    const req = httpMock.expectOne(mockApiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
  });
});
