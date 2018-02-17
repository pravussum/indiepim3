import {Injectable} from '@angular/core';
import 'rxjs/add/operator/catch';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Credentials} from "./credentials";
import {Subject} from "rxjs/Subject";
import {Router} from "@angular/router";

@Injectable()
export class LoginService {

  private loggedInSource = new Subject<string>();

  loggedInAnnounced$ = this.loggedInSource.asObservable();

  constructor(private http: HttpClient, private router: Router) {

  }

  login(credentials: Credentials) {

    let headers = new HttpHeaders();
    headers = headers.append('Authorization', 'Basic ' + btoa(credentials.user + ':' + credentials.password));
    headers = headers.append('X-Requested-With','XMLHttpRequest'); // to suppress 401 browser popup

    this.http.post('/api/login',{}, {headers})
      .subscribe(
        (data) => {
          let user = data['principal']['username'];
          this.loggedInSource.next(user);
          localStorage.setItem('currentUser', user);
        },
        err => console.log(err),
        () => this.router.navigate((['/frontend/maillist']))
      );
  }

  logout() {
    this.http.get('/api/logout').subscribe(
      () => {
        this.loggedInSource.next(null);
        localStorage.removeItem('currentUser');
        console.log('logged out.');
        this.router.navigate((['/frontend/login']));
      },
      (err) => console.error(err)
    );
  }
}
