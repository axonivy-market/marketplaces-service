import { SafeHtml } from "@angular/platform-browser";

export interface ProductReleaseSafeHtml {
    name: string;
    body: SafeHtml;
    publishedAt: string;
}