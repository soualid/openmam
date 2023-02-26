import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.sass']
})
export class LoginComponent {

  login:string = ""
  password:string = ""

  constructor(private userService: UserService,
              private router: Router) {
    
  }

  doLogin(event:any):void {
    event.preventDefault()
    this.userService.doLogin(this.login, this.password).subscribe(result => {
      console.log('login', result)
      if (result.accessToken) {
        this.router.navigate(['home']);
      }
    })
  }

}
