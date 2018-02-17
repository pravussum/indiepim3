import {Component, OnInit} from '@angular/core';
import {LoginService} from "../login/login.service";
import {Subscription} from "rxjs/Subscription";

@Component({
  selector: 'app-navpanel',
  templateUrl: './navpanel.component.html',
  styleUrls: ['./navpanel.component.css']
})
export class NavpanelComponent implements OnInit {

  private user: string;
  subscription: Subscription;

  constructor(private loginService: LoginService) { }

  ngOnInit() {
    this.subscription = this.loginService.loggedInAnnounced$.subscribe(user => {
        this.user = user
      }
    );
    if(!this.user) {
      console.log("Loading current user from local storage");
      this.user = localStorage.getItem('currentUser');
    }
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  logout() {
    this.loginService.logout();
  }

}
