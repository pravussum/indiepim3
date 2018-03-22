import {Component, OnInit} from '@angular/core';
import {UserService} from "../user.service";

@Component({
  selector: 'app-usersettings',
  templateUrl: './usersettings.component.html',
  styleUrls: ['./usersettings.component.css'],
  providers: [UserService]
})
export class UsersettingsComponent implements OnInit {

  password: string;

  constructor(private userService: UserService) { }

  ngOnInit() {

  }

  updatePassword() {
    this.userService.updatePassword(this.password).subscribe(
      data => console.log("password updated"), error => console.error("password update failed")
    );
  }
}
