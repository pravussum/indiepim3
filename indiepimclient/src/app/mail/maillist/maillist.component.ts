import { Component, OnInit } from '@angular/core';
import {MailListItem} from "./maillistitem";
import {MaillistService} from "./maillist.service";
import {SmartDatePipe} from "../../pipes/smart-date.pipe";

@Component({
  selector: 'app-maillist',
  templateUrl: './maillist.component.html',
  styleUrls: ['./maillist.component.css'],
  providers: [
    MaillistService,
    SmartDatePipe]
})
export class MaillistComponent implements OnInit {

  mailListItems: MailListItem[];
  loading: boolean;
  currentViewDisplayText: string;

  constructor(private maillistService: MaillistService) { }

  ngOnInit() {
    this.maillistService.getMessages().subscribe(data => this.mailListItems = data.messages);
  }

  viewSelectedMail(mailListItem: MailListItem) {
    console.log("Viewing mail " + mailListItem.msgId)
  }


  deleteMessage(mailListItem: MailListItem) {
    console.log("Deleting mail " + mailListItem.msgId)
  }

  toggleRead(mailListItem: MailListItem) {
    console.log("Toggle read " + mailListItem.msgId)
  }
}
