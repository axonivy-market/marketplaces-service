import { Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { ThemeService } from '../../../core/services/theme/theme.service';
import { LanguageService } from '../../../core/services/language/language.service';
import { CommonModule } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-error-page-component',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './error-page.component.html',
  styleUrl: './error-page.component.scss'
})
export class ErrorPageComponent implements OnInit {
  themeService = inject(ThemeService);
  languageService = inject(LanguageService);
  translateService = inject(TranslateService);
  isMobileMode = signal<boolean>(false);
  route = inject(ActivatedRoute);
  errorMessageKey = '';
  errorId: string | undefined;

  constructor(private readonly router: Router) {
    this.checkMediaSize();
  }

  ngOnInit(): void {
    this.errorId = this.route.snapshot.params['id'];
    this.translateService
      .get('common.error.description')
      .subscribe(errorTranslations => {
        let i18nErrorKey;
        if (
          this.errorId &&
          Object.keys(errorTranslations).includes(this.errorId) &&
          this.errorId !== 'default'
        ) {
          i18nErrorKey = this.errorId;
        } else {
          i18nErrorKey = 'default';
        }
        this.errorMessageKey = this.buildI18nKey(i18nErrorKey);
      });
  }

  private buildI18nKey(key: string | undefined) {
    if (key) {
      return `common.error.description.${key}`;
    }
    return '';
  }

  backToHomePage() {
    this.router.navigate(['/']);
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkMediaSize();
  }

  checkMediaSize() {
    const mediaQuery = window.matchMedia('(max-width: 767px)');
    this.isMobileMode.set(mediaQuery.matches);
  }

  getImageSrcInLightMode(): string {
    if (this.isMobileMode()) {
      return '/assets/images/misc/robot-mobile.png';
    }

    return '/assets/images/misc/robot.png';
  }

  getImageSrcInDarkMode(): string {
    if (this.isMobileMode()) {
      return '/assets/images/misc/robot-mobile-black.png';
    }

    return '/assets/images/misc/robot-black.png';
  }
}
