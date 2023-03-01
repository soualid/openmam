import { Injectable } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { User } from '../models/user';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { Page } from '../models/page';
import { UploadRequest, UploadRequestStatus } from '../models/upload.request';


@Injectable({
  providedIn: 'root'
})
export class PartnerUploadService {

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient,
    private router: Router) { 
    }

  createUploadRequest<Data>(partnerId:number, mediaId:number): Observable<User> {
    const url = `/api/partner/${partnerId}/media/${mediaId}/uploadRequest`;
    return this.http.post<UploadRequest>(url, {})
      .pipe(
        catchError(this.handleError<User>(`createUploadRequest`))
      ).pipe(
        map(result => {
          console.log('createUploadRequest', result)
          return result
        })
      );
  }


  updateUploadRequestStatus<Data>(requestId:number, newStatus:UploadRequestStatus): Observable<User> {
    const url = `/api/uploadRequests/${requestId}/status/${newStatus}`;
    return this.http.put<UploadRequest>(url, {})
      .pipe(
        catchError(this.handleError<User>(`createUploadRequest`))
      ).pipe(
        map(result => {
          console.log('createUploadRequest', result)
          return result
        })
      );
  }

  getUploadRequestsForConnectedUser<Data>(): Observable<Page<UploadRequest>> {
    const url = `/api/my/uploadRequests`;
    return this.http.get<Page<UploadRequest>>(url, {})
      .pipe(
        catchError(this.handleError<Page<UploadRequest>>(`getUploadRequestsForConnectedUser`))
      ).pipe(
        map(result => {
          console.log('getUploadRequestsForConnectedUser', result)
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
