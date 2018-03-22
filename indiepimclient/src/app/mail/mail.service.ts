import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";
import {HttpClient} from "@angular/common/http";

@Injectable()
export class MailService {

  constructor(private http: HttpClient) { }

  getEmailAdresses(): Observable<any> {
    return this.http.get('/api/command/getEmailAddresses');
  }

}
