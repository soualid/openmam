import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { MetadataAttachmentType, MetadataGroup, MetadataReference } from '../models/metadata';
import { Page } from '../models/page';

@Injectable({ providedIn: 'root' })
export class MetadataService {

  private metadataGroupsUrl = 'api/metadataGroups';
  private metadataGroupUrl = 'api/metadataGroup';
  private metadataUrl = 'api/metadata';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient) { }

  getMetadataGroups(): Observable<Page<MetadataGroup>> {
    return this.http.get<Page<MetadataGroup>>(this.metadataGroupsUrl)
      .pipe(
        tap(_ => this.log('fetched metadata groups')),
        catchError(this.handleError<Page<MetadataGroup>>('getMetadataGroups', {}))
      );
  }

  getMetadataGroup<Data>(id: number): Observable<MetadataGroup> {
    const url = `${this.metadataGroupUrl}/${id}`;
    return this.http.get<MetadataGroup>(url)
      .pipe(
        tap(h => {
          const outcome = h ? 'fetched' : 'did not find';
          console.log(`${outcome} media id=${id}`);
        }),
        catchError(this.handleError<MetadataGroup>(`getMetadataGroup id=${id}`))
      );
  }

  getAutocompleteData<Data>(id:number): Observable<MetadataReference[]> {
    const url = `${this.metadataGroupUrl}/${id}/autocomplete`;
    return this.http.get<MetadataReference[]>(url)
      .pipe(
        tap(h => {
          const outcome = h ? 'fetched' : 'did not find';
          console.log(`${outcome} media id=${id}`);
        }),
        catchError(this.handleError<MetadataReference[]>(`getAutocompleteData id=${id}`))
      );
  }

  persistMetadatasForEntity<Data>(datas:any): Observable<Data> {
    const url = `${this.metadataUrl}`;
    return this.http.post<Data>(url, datas)
      .pipe(
        catchError(this.handleError<Data>(`persistMetadatasForEntity title=${name}`))
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
