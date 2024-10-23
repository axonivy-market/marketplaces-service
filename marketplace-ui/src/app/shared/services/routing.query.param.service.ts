import { computed, Injectable, signal } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { DESIGNER_COOKIE_VARIABLE } from '../constants/common.constant';
import { Router, Params, NavigationStart, Route, ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter } from 'rxjs/operators';
@Injectable({
  providedIn: 'root'
})
export class RoutingQueryParamService {
  private readonly isDesigner = signal(false);
  isDesignerEnv = computed(() => this.isDesigner());
  designerVersion = signal('');

  constructor(
    private readonly cookieService: CookieService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {
    this.getNavigationStartEvent().subscribe(() => {
      if (!this.isDesigner()) {
        this.isDesigner.set(
          this.cookieService.get(
            DESIGNER_COOKIE_VARIABLE.ivyViewerParamName
          ) === DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer
        );

        this.route.queryParams.subscribe(params => {
          this.checkCookieForDesignerEnv(params);
          this.checkCookieForDesignerVersion(params);
        });
      }
    });
  }

  checkCookieForDesignerVersion(params: Params) {
    const versionParam = params[DESIGNER_COOKIE_VARIABLE.ivyVersionParamName];
    // console.log(versionParam);

    // if (versionParam !== undefined) {
    //   this.cookieService.set(
    //     DESIGNER_COOKIE_VARIABLE.ivyVersionParamName,
    //     versionParam
    //   );
    //   this.designerVersion.set(versionParam);
    // }

    if (versionParam !== undefined) {
      if (Array.isArray(versionParam)) {
        let versionParamSet = Array.from(new Set(versionParam));
        this.cookieService.set(
          DESIGNER_COOKIE_VARIABLE.ivyVersionParamName,
          versionParamSet[0]
        );
        this.designerVersion.set(versionParamSet[0]);
      } else {
        this.cookieService.set(
          DESIGNER_COOKIE_VARIABLE.ivyVersionParamName,
          versionParam
        );
        this.designerVersion.set(versionParam);
      }
    }
  }

  checkCookieForDesignerEnv(params: Params) {
    const ivyViewerParam = params[DESIGNER_COOKIE_VARIABLE.ivyViewerParamName];
    if (ivyViewerParam === DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer) {
      this.cookieService.set(
        DESIGNER_COOKIE_VARIABLE.ivyViewerParamName,
        ivyViewerParam
      );
      this.isDesigner.set(true);
    }
  }

  getDesignerVersionFromCookie() {
    if (this.designerVersion() === '') {
      this.designerVersion.set(
        this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyVersionParamName)
      );
    }
    return this.designerVersion();
  }

  isDesignerViewer() {
    if (!this.isDesigner()) {
      this.isDesigner.set(
        this.cookieService.get(DESIGNER_COOKIE_VARIABLE.ivyViewerParamName) ===
          DESIGNER_COOKIE_VARIABLE.defaultDesignerViewer
      );
    }
    return this.isDesigner();
  }

  getNavigationStartEvent(): Observable<NavigationStart> {
    return this.router.events.pipe(
      filter(event => event instanceof NavigationStart)
    ) as Observable<NavigationStart>;
  }
}
