import { Injectable } from '@angular/core';

import { HttpClient, HttpEvent, HttpEventType, HttpHeaders, HttpParams, HttpRequest } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class S3UploaderService {

  constructor(private http: HttpClient) { }

  uploadfileAWSS3(url:string, contentType:string, file:File) { 
 
    const headers = new HttpHeaders({ 'Content-Type': contentType });
    const req = new HttpRequest(
      'PUT',
      url,
      file,
      {
        headers,
        reportProgress: true
      })
    return this.http.request(req)
  }
}