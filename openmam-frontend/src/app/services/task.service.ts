import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { Page } from '../models/page';
import { Task } from '../models/task';

@Injectable({ providedIn: 'root' })
export class TaskService {

  private tasksUrl = 'api/tasks'

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
  };

  constructor(
    private http: HttpClient) { }

  getTasks(): Observable<Page<Task>> {
    return this.http.get<Page<Task>>(`${this.tasksUrl}?sort=creationDate,desc`)
      .pipe(
        tap(_ => this.log('fetched tasks')),
        catchError(this.handleError<Page<Task>>('tasks', {}))
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
    console.log(`TaskService: ${message}`);
  }
}
