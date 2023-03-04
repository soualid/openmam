import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { CreateMediaResult, CreateVersionResult, MediaVersion, Media } from '../models/media';
import { Page } from '../models/page';
import { Task } from '../models/task';

@Injectable({ providedIn: 'root' })
export class MediaService {
  
  private mediasUrl = 'api/medias';
  private dashboardUrl = 'api/dashboard';
  private mediaUrl = 'api/media';

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

  getDashboard(): Observable<Page<Media>> {
    return this.http.get<Page<Media>>(this.dashboardUrl)
      .pipe(
        tap(_ => this.log('fetched getDashboard')),
        catchError(this.handleError<Page<Media>>('getDashboard', {}))
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

  triggerMediaMove<Data>(id: number, locationId: number): Observable<void> {
    const url = `${this.mediaUrl}/${id}/move/${locationId}`;
    return this.http.post<void>(url, {})
      .pipe(
        catchError(this.handleError<void>(`triggerMediaMove id=${id}`))
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


  getPendingTaskForMedia<Data>(id: number): Observable<Task[]> {
    const url = `${this.mediaUrl}/${id}/tasks/pending`;
    return this.http.get<Task[]>(url)
      .pipe(
        tap(h => {
          const outcome = h ? 'fetched' : 'did not find';
          console.log(`${outcome} media id=${id}`);
        }),
        catchError(this.handleError<Task[]>(`getPendingTaskForMedia id=${id}`))
      );
  }  

  createVersion<Data>(version:MediaVersion, mediaId:number, audioId: number): Observable<Media> {
    const url = `${this.mediaUrl}/${mediaId}/version?audioId=${audioId}`;
    return this.http.post<CreateVersionResult>(url, version)
      .pipe(
        catchError(this.handleError<CreateVersionResult>(`CreateVersionResult version=${version}`))
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
