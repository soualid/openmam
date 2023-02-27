import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.sass']
})
export class HomeComponent {

  constructor(private router: Router,
              private userService: UserService) {
    if (!userService.currentUser) {
      this.router.navigate(['login']);
    }
  }

}
