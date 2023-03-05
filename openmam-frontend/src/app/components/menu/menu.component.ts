import { Component } from '@angular/core';
import { User } from 'src/app/models/user';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.sass']
})
export class MenuComponent {
  
  currentUser: User | undefined;

  constructor(private userService: UserService) {
    this.currentUser = userService.currentUser
    console.log('current user menu', this.currentUser)
    userService.execChange.subscribe((value) => {
      console.log('menu', this.currentUser)
      this.currentUser = value
    })
  }

  intersect(a:any[]|undefined, b:any[]|undefined) {
    if (!a || !b) return []
    var setB = new Set(b);
    return [...new Set(a)].filter(x => setB.has(x));
  }
  

  logout(event:any) {
    event.preventDefault()
    this.userService.doLogout()
  }
    
  links = [{
    title: 'Home',
    icon: 'home',
    target: '/home',
    roles: ['ROLE_PARTNER', 'ROLE_USER', 'ROLE_ADMIN']
  },
  {
    title: 'My dashboard',
    icon: 'dashboard',
    target: '/dashboard',
    roles: ['ROLE_USER', 'ROLE_ADMIN']
  },
  {
    title: 'Medias',
    icon: 'play_circle',
    target: '/search',
    roles: ['ROLE_USER', 'ROLE_ADMIN']
  },
  {
    title: 'Locations',
    icon: 'folder_copy',
    target: '/locations',
    roles: ['ROLE_ADMIN']
  },
  {
    title: 'Tasks',
    icon: 'task',
    target: '/tasks',
    roles: ['ROLE_ADMIN']
  },
  {
    title: 'Metadata schemas',
    icon: 'extension',
    target: '/metadata_schema',
    roles: ['ROLE_ADMIN']
  },
  {
    title: 'Partner upload',
    icon: 'upload',
    target: '/partner_upload',
    roles: ['ROLE_PARTNER']
  },
  {
    title: 'Users',
    icon: 'group',
    target: '/users',
    roles: ['ROLE_ADMIN']
  }]
}
