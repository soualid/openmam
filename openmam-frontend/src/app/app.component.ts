import { Component } from '@angular/core';
import { User } from './models/user';
import { UserService } from './services/user.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.sass'],
})
export class AppComponent {
  title = 'openmam-frontend';
  currentUser: User|undefined;

  constructor(private userService: UserService) {
    this.currentUser = userService.currentUser
    userService.execChange.subscribe((value) => {
      this.currentUser = value
    })
  }


}
