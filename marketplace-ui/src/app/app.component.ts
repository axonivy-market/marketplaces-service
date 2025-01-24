import { FooterComponent } from './shared/components/footer/footer.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { RoutingQueryParamService } from './shared/services/routing.query.param.service';
import { CommonModule } from '@angular/common';
import { ERROR_PAGE_PATH } from './shared/constants/common.constant';
import { Component, ElementRef, inject, Renderer2 } from '@angular/core';
import {
  ActivatedRoute,
  NavigationError,
  Router,
  RouterOutlet,
  Event
} from '@angular/router';
import { BackToTopComponent } from "./shared/components/back-to-top/back-to-top.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, CommonModule, BackToTopComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  routingQueryParamService = inject(RoutingQueryParamService);
  route = inject(ActivatedRoute);
  isMobileMenuCollapsed = true;

  constructor(private readonly router: Router, private renderer: Renderer2, private elRef: ElementRef) {}

  ngOnInit(): void {
    this.router.events.subscribe((event: Event) => {
      if (event instanceof NavigationError) {
        this.router.navigate([ERROR_PAGE_PATH]);
      }
    });

    this.routingQueryParamService.getNavigationStartEvent().subscribe(() => {
      if (!this.routingQueryParamService.isDesignerEnv()) {
        this.route.queryParams.subscribe(params => {
          this.routingQueryParamService.checkSessionStorageForDesignerEnv(params);
          this.routingQueryParamService.checkSessionStorageForDesignerVersion(params);
        });
      }
    });
  }
}
