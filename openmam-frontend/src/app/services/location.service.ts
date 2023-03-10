import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { Page } from '../models/page';
import { Location } from '../models/media';

@Injectable({ providedIn: 'root' })
export class LocationService {

  private locationsUrl = 'api/locations'

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient) { }

  getLocations(): Observable<Page<Location>> {
    return this.http.get<Page<Location>>(this.locationsUrl)
      .pipe(
        tap(_ => this.log('fetched locations')),
        catchError(this.handleError<Page<Location>>('locations', {}))
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
    console.log(`MetadataService: ${message}`);
  }
}
