import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class UserService {

  constructor(private http: HttpClient) { }

  updatePassword(password: string): Observable<any> {
    console.log(JSON.stringify(password));
    return this.http.post('/api/user/updatePassword', password);
  }


}
