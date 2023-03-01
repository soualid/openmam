import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { User } from '../models/user';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Page } from '../models/page';


@Injectable({
  providedIn: 'root'
})
export class UserService {

  execChange: Subject<any> = new Subject<User>();
  currentUser:User|undefined;

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient,
    private router: Router) { 
      const auth = localStorage.getItem('auth')
      if (auth && auth != "undefined") {
        this.currentUser = JSON.parse(auth)
        this.userChanged(this.currentUser!)
      }
    }

  userChanged(data: User|undefined) {
    this.execChange.next(data);
  }

  getUsers(): Observable<Page<User>> {
    return this.http.get<Page<User>>(`/api/users?sort=id,desc`)
      .pipe(
        tap(_ => this.log('fetched users')),
        catchError(this.handleError<Page<User>>('users', {}))
      );
  }

  getAutocompleteData<Data>(roleName:string, prefix:string): Observable<User[]> {
    const url = `/api/users/role/${roleName}?prefix=${prefix}`;
    return this.http.get<User[]>(url)
      .pipe(
        tap(h => {
          const outcome = h ? 'fetched' : 'did not find';
          console.log(`${outcome} media prefix=${prefix}`);
        }),
        catchError(this.handleError<User[]>(`getAutocompleteData prefix=${prefix}`))
      );
  }

  doLogout() {
    this.currentUser = undefined
    this.userChanged(this.currentUser)
    localStorage.removeItem('auth')
    this.router.navigate(['login']);
  }

  doLogin<Data>(login:string, password:string): Observable<User> {
    const url = `/api/authenticate`;
    return this.http.post<User>(url, {
      login, password
    })
      .pipe(
        catchError(this.handleError<User>(`doLogin`))
      ).pipe(
        map(result => {
          console.log('userChanged', result)
          this.currentUser = result
          this.userChanged(result)
          localStorage.setItem('auth', JSON.stringify(this.currentUser))
          return result
        })
      );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   *
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  private log(message: string) {
    console.log(`UserService: ${message}`);
  }
}
