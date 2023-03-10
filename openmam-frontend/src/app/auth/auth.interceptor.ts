import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import {tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import { UserService } from '../services/user.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router,
              private userService:UserService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const currentUser = this.userService.currentUser
    console.log('request', request)
    
    if (currentUser && currentUser.accessToken 
        && request.url.indexOf("amazonaws.com") === -1) {
      request = request.clone({
        setHeaders: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${currentUser.accessToken}`
        }
      });
    }

    return next.handle(request).pipe( tap(() => {},
      (err: any) => {
      if (err instanceof HttpErrorResponse) {
        if (err.status !== 401 && err.status !== 403) {
          return;
        }

        this.userService.currentUser = undefined
        this.userService.userChanged(undefined)
        this.router.navigate(['login']);
      }
    }));
  }
}
