import { Injectable } from '@angular/core';
import {Observable} from "rxjs/Observable";
import {HttpClient} from "@angular/common/http";
import {MailListResult} from "./maillistitem";
import {MaillistView} from "./maillistview";

@Injectable()
export class MaillistService {

  constructor(private http: HttpClient) {
  }

  getMessages(mailListView: MaillistView, pageSize?: number): Observable<MailListResult> {
    return this.http.post<MailListResult>("/api/command/getMessages", {
      accountId: mailListView.accountId,
      tagName: mailListView.tagName,
      tagLineageId: mailListView.tagLineageId,
      searchTerm:  mailListView.searchTerm,
      read: mailListView.read,
      offset: mailListView.offset,
      pageSize: pageSize
    });
  }
}
