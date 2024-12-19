import { Pipe, PipeTransform } from '@angular/core';
import { Language } from '../enums/language.enum';
import { DisplayValue } from '../models/display-value.model';
import MarkdownIt from 'markdown-it';
// import MarkdownItGitHubAlerts from 'markdown-it-github-alerts/index';

@Pipe({
  standalone: true,
  name: 'multilingualism'
})
export class MultilingualismPipe implements PipeTransform {
  transform(value: DisplayValue | null, language: Language, _args?: []): string {
    let displayValue = '';
    if (value) {
      displayValue = value[language];
      if (displayValue === undefined || displayValue === '') {
        displayValue = value[Language.EN];
      }
    }
    return displayValue;
    // return this.renderGithubAlerts(displayValue);
  }

  // public renderGithubAlerts(value: string) {
  //   const md = MarkdownIt()

  //   md.use(MarkdownItGitHubAlerts, /* Options */)

  //   const html = md.render(value)
  //   return html;
  // }
}
