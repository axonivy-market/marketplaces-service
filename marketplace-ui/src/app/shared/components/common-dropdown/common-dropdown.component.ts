import { Component, ElementRef, EventEmitter, HostListener, inject, Input, Output } from '@angular/core';
import { NgClass } from '@angular/common';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ItemDropdown } from '../../models/item-dropdown.model';

@Component({
  selector: 'app-common-dropdown',
  standalone: true,
  imports: [
    NgClass,
    TranslateModule
  ],
  templateUrl: './common-dropdown.component.html',
  styleUrl: './common-dropdown.component.scss'
})
export class CommonDropdownComponent<T extends string> {
  translateService = inject(TranslateService);

  @Input() items: ItemDropdown<T>[] = [];
  @Input() selectedItem: T | undefined;
  @Input() buttonClass = '';
  @Input() ariaLabel = '';

  @Output() itemSelected = new EventEmitter<ItemDropdown<T>>();
  elementRef = inject(ElementRef);
  isDropdownOpen = false;
  @Input() meta_data_json_url: string | undefined = '';

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  onSelect(item: ItemDropdown<T>) {
    this.itemSelected.emit(item);
    this.isDropdownOpen = false;
    this.meta_data_json_url = item.meta_data_json_url;
  }

  isActiveItem(value: ItemDropdown, selectedItem: T | undefined): boolean {
    return this.translateService.instant(value.label) === selectedItem;
  }

  @HostListener('document:click', ['$event'])
  handleClickOutside(event: MouseEvent) {
    if (!this.elementRef.nativeElement.querySelector('.dropdown').contains(event.target) && this.isDropdownOpen) {
      this.isDropdownOpen = !this.isDropdownOpen;
    }
  }

}
