import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";
import {MessageAccount} from "../accounts/messageaccount";
import {HttpClient} from "@angular/common/http";
import {MailListResult} from "./maillistitem";

@Injectable()
export class MaillistService {

  constructor(private http: HttpClient) {
  }

  getMessages(): Observable<MailListResult> {
    return this.http.post<MailListResult>("/api/command/getMessages", {pageSize: 50});
  }
}
