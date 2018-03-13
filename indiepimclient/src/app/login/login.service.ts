import {Injectable} from '@angular/core';
import 'rxjs/add/operator/catch';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Credentials} from "./credentials";
import {Subject} from "rxjs/Subject";
import {Router} from "@angular/router";

@Injectable()
export class LoginService {

  private loggedInSource = new Subject<string>();
  private ws : WebSocket;
  loggedInAnnounced$ = this.loggedInSource.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.initWebsocket();
  }

  private initWebsocket() {
    console.log("connecting via websocket...");
    this.ws = new WebSocket(window.location.protocol.replace('http', 'ws') + '//' + window.location.host + '/ws/messages');
    this.ws.onmessage = (msgEvent: MessageEvent) => console.log("websocket message : " + msgEvent.data);
    this.ws.onopen = () => {
      console.log("websocket open")
    };
    this.ws.onclose = () => {
      console.log("websocket connection closed.")
    };
    this.ws.onerror = (ev: Event) => console.dir(ev);
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
          this.initWebsocket();
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
        this.ws = undefined
        this.router.navigate((['/frontend/login']));
      },
      (err) => console.error(err)
    );
  }

  socketMessage(data: any) {
    if(this.ws === undefined) {
      console.log("Websocket connection not initialized...");
      return;
    }
    if(this.ws.readyState === WebSocket.OPEN) {
      console.log("sending websocket message");
      this.ws.send(data);
    } else {
      console.error("Websocket not in readyState OPEN.");
    }
  }

}
