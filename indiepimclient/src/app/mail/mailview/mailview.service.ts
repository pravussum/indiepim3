import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import {MailItem} from "./mailItem";

@Injectable()
export class MailviewService {

  constructor(private http: HttpClient) { }

  getMessage(id: number) : Observable<MailItem>{
    return this.http.get<MailItem>("/api/command/getMessage/" + id);
  }
}
