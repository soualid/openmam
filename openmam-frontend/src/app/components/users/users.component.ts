import { Component } from '@angular/core';
import { Page } from 'src/app/models/page';
import { User } from 'src/app/models/user';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.sass']
})
export class UsersComponent {

  result: Page<User> = {}
  loading: boolean = true
  dataSource: User[] = []
  columnsToDisplay: string[] = ['id', 'email', 'firstName', 'lastName', 'roles']

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.getUsers();
  }
  
  getUsers(): void {
    this.userService.getUsers()
      .subscribe(result => {
        this.result = result
        this.loading = false
        this.dataSource = this.result.content ?? []
        console.log('set datasource to', this.dataSource)
      });
  }

}
