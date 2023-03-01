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

  logout(event:any) {
    event.preventDefault()
    this.userService.doLogout()
  }
    
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
  },
  {
    title: 'Partner upload',
    icon: 'upload',
    target: '/partner_upload'
  },
  {
    title: 'Users',
    icon: 'group',
    target: '/users'
  }]
}
