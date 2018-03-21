import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {SendMessage} from "./sendmessage";
import {Observable} from "rxjs/Observable";

@Injectable()
export class SendService {

  constructor(private http: HttpClient) { }

  send(message: SendMessage) :  Observable<any> {
    console.log("sending");
    return this.http.post('/api/command/sendMessage', message);
  }

}
