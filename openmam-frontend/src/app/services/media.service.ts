import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { CreateMediaResult, Media } from '../models/media';
import { Page } from '../models/page';

@Injectable({ providedIn: 'root' })
export class MediaService {

  private mediasUrl = 'api/medias';  // URL to web api
  private mediaUrl = 'api/media';  // URL to web api

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient) { }

  getMedias(): Observable<Page<Media>> {
    return this.http.get<Page<Media>>(this.mediasUrl)
      .pipe(
        tap(_ => this.log('fetched medias')),
        catchError(this.handleError<Page<Media>>('getMedias', {}))
      );
  }

  triggerMediaScan<Data>(id: number, path: string, locationId: number = 1): Observable<void> {
    const url = `${this.mediaUrl}/${id}/scan`;
    return this.http.post<void>(url, {
      locationId,
      path
    })
      .pipe(
        catchError(this.handleError<void>(`triggerMediaScan id=${id}`))
      );
  }

  createMedia<Data>(name: string): Observable<CreateMediaResult> {
    const url = `${this.mediaUrl}`;
    return this.http.post<CreateMediaResult>(url, {
      name
    })
      .pipe(
        catchError(this.handleError<CreateMediaResult>(`CreateMediaResult title=${name}`))
      );
  }    

  getMedia<Data>(id: number): Observable<Media> {
    const url = `${this.mediaUrl}/${id}`;
    return this.http.get<Media>(url)
      .pipe(
        tap(h => {
          const outcome = h ? 'fetched' : 'did not find';
          console.log(`${outcome} media id=${id}`);
        }),
        catchError(this.handleError<Media>(`getMedia id=${id}`))
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
    console.log(`MediaService: ${message}`);
  }
}
