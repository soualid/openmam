import { Component } from '@angular/core';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.sass']
})
export class MenuComponent {
  links = [{
    title: 'Home',
    icon: 'home',
    target: '/home'
  },
  {
    title: 'Medias',
    icon: 'play_circle',
    target: '/search'
  },
  {
    title: 'Locations',
    icon: 'folder_copy',
    target: '/locations'
  },
  {
    title: 'Tasks',
    icon: 'task',
    target: '/tasks'
  },
  {
    title: 'Metadata schemas',
    icon: 'extension',
    target: '/metadata_schema'
  }]
}
