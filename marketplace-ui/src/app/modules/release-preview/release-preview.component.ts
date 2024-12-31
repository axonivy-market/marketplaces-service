import {
  Component,
  inject,
  ViewEncapsulation,
  Signal,
  WritableSignal,
  computed,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ReleasePreviewService } from './release-preview.service';
import { ReleasePreviewData } from '../../shared/models/release-preview-data.model';
import { LanguageService } from '../../core/services/language/language.service';
import { ThemeService } from '../../core/services/theme/theme.service';
import { CommonUtils } from '../../shared/utils/common.utils';
import { PRODUCT_DETAIL_TABS } from '../../shared/constants/common.constant';
import { ItemDropdown } from '../../shared/models/item-dropdown.model';
import { CommonDropdownComponent } from '../../shared/components/common-dropdown/common-dropdown.component';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { SafeHtml, Title, DomSanitizer } from '@angular/platform-browser';
import MarkdownIt from 'markdown-it';
import MarkdownItGitHubAlerts from 'markdown-it-github-alerts';
import { full } from 'markdown-it-emoji';
import { DisplayValue } from '../../shared/models/display-value.model';
import { MultilingualismPipe } from '../../shared/pipes/multilingualism.pipe';

const DEFAULT_ACTIVE_TAB = 'description';
const MAX_FILE_SIZE_MB = 20;
@Component({
  selector: 'app-release-preview',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CommonDropdownComponent,
    TranslateModule,
    MultilingualismPipe
  ],
  templateUrl: './release-preview.component.html',
  styleUrls: ['./release-preview.component.scss'],
  encapsulation: ViewEncapsulation.Emulated
})
export class ReleasePreviewComponent {
  selectedFile: File | null = null;
  activeTab = DEFAULT_ACTIVE_TAB;
  selectedLanguage = 'en';
  isZipFile = false;
  isUploaded = false;
  shouldShowHint = false;
  readmeContent: WritableSignal<ReleasePreviewData> = signal(
    {} as ReleasePreviewData
  );
  languageService = inject(LanguageService);
  themeService = inject(ThemeService);
  translateService = inject(TranslateService);
  private readonly titleService = inject(Title);
  detailTabs = PRODUCT_DETAIL_TABS;
  displayedTabsSignal: Signal<ItemDropdown[]> = computed(() => {
    this.languageService.selectedLanguage();
    return this.getDisplayedTabsSignal();
  });
  private readonly sanitizer = inject(DomSanitizer);

  private readonly releasePreviewService = inject(ReleasePreviewService);

  ngOnInit(): void {
    // Set the title initially
    this.updateHomePageTitle();

    // Update the title whenever the language changes
    this.translateService.onLangChange.subscribe(() => {
      this.updateHomePageTitle();
    });
  }

  private updateHomePageTitle(): void {
    this.translateService
      .get('common.preview.pageTitle')
      .subscribe((translatedTitle: string) => {
        this.titleService.setTitle(translatedTitle);
      });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const maxFileSize = MAX_FILE_SIZE_MB * 1024 * 1024;
      this.selectedFile = file;

      // Check if the selected file is a ZIP file
      this.isZipFile =
        file.type === 'application/zip' ||
        (file.name.endsWith('.zip') && file.size < maxFileSize);
    }
  }

  toggleHint() {
    this.shouldShowHint = !this.shouldShowHint;
  }

  onSubmit(): void {
    this.handlePreviewPage();
  }

  setActiveTab(event: string): void {
    this.activeTab = event;
  }

  handlePreviewPage(): void {
    if (!this.selectedFile || !this.isZipFile) {
      return;
    }
    this.releasePreviewService.extractZipDetails(this.selectedFile).subscribe({
      next: response => {
        this.readmeContent.set(response);
        this.isUploaded = true;
        this.shouldShowHint = false;
      }
    });
  }

  getDisplayedTabsSignal() {
    const displayedTabs: ItemDropdown[] = [];
    for (const detailTab of this.detailTabs) {
      if (this.getContent(detailTab.value)) {
        displayedTabs.push(detailTab);
        this.activeTab = displayedTabs[0].value;
      }
    }
    return displayedTabs;
  }

  getContent(value: string): boolean {
    const content = this.readmeContent();

    const tabContent = content?.[value as keyof ReleasePreviewData];
    return (
      !!tabContent &&
      Object.keys(tabContent).length > 0 &&
      CommonUtils.isContentDisplayedBasedOnLanguage(
        tabContent,
        this.languageService.selectedLanguage()
      )
    );
  }

  getSelectedTabLabel() {
    return CommonUtils.getLabel(this.activeTab, PRODUCT_DETAIL_TABS);
  }

  getReadmeContentValue(key: ItemDropdown): DisplayValue | null {
    type tabName = 'description' | 'demo' | 'setup';
    const value = key.value as tabName;
    return this.readmeContent()[value];
  }

  renderReadmeContent(value: string): SafeHtml {
    const md = MarkdownIt();
    md.use(full);
    md.use(MarkdownItGitHubAlerts);
    const result = md.render(value);
    return this.sanitizer.bypassSecurityTrustHtml(result);
  }
}
