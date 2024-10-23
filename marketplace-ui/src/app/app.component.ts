import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { LoadingService } from './core/services/loading/loading.service';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { CommonModule } from '@angular/common';
import { DESIGNER_COOKIE_VARIABLE, ERROR_PAGE_PATH } from './shared/constants/common.constant';
import { Component, inject } from '@angular/core';
import {
  ActivatedRoute,
  NavigationError,
  Router,
  RouterOutlet,
  Event
} from '@angular/router';
import { LoadingSpinnerComponent } from "./shared/components/loading-spinner/loading-spinner.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, CommonModule, LoadingSpinnerComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  loadingService = inject(LoadingService);
  routingQueryParamService = inject(RoutingQueryParamService);
  route = inject(ActivatedRoute);
  isMobileMenuCollapsed = true;

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.router.events.subscribe((event: Event) => {
      if (event instanceof NavigationError) {
        this.router.navigate([ERROR_PAGE_PATH]);
      }
    });

    // this.routingQueryParamService.getNavigationStartEvent().subscribe(() => {
    //   if (!this.routingQueryParamService.isDesignerEnv()) {
    //     this.route.queryParams.subscribe(params => {
    //       this.routingQueryParamService.checkCookieForDesignerEnv(params);
    //       this.routingQueryParamService.checkCookieForDesignerVersion(params);
    //     });
    //   }
    // });
  }
}
