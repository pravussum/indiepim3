import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MessageAccount} from "./messageaccount";
import {Observable} from "rxjs/Observable";

@Injectable()
export class AccountsService {

  constructor(private http: HttpClient) { }

  getAccounts() : Observable<MessageAccount[]>{
    return this.http.get<MessageAccount[]>("/api/command/getMessageAccounts");
  }

  removeAccount(account: MessageAccount) : Observable<any>{
    return this.http.post('/api/command/deleteMessageAccount', account);

  }

  saveAccount(account: MessageAccount) : Observable<any> {
    return this.http.post('/api/command/createOrUpdateMessageAccount', account);
  }

  startIncSync(account: MessageAccount) : Observable<any> {
    return this.http.get('/api/account/' + account.id + "/startIncSync")
  }

}
