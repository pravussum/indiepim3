import { Component, OnInit } from '@angular/core';
import { LoginService} from './login.service';
import {Credentials} from "./credentials";
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {

  user: string;
  credentials = new Credentials();
  loggedInSub : Subscription;
  constructor(private loginService: LoginService) { }

  ngOnInit() {
    this.loginService.loggedInAnnounced$.subscribe((user) => {
      this.user = user;
    });
    if(!this.user) {
      console.log("Loading current user from local storage");
      this.user = localStorage.getItem('currentUser');
      console.log("User found in local storage:");
      console.dir(this.user);
    }
  }

  ngOnDestroy() {
    if(this.loggedInSub) {
      this.loggedInSub.unsubscribe();
    }
  }

  loginNow() {
    this.loginService.login(this.credentials);
  }

}
