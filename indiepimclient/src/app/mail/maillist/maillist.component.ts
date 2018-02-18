import { Component, OnInit } from '@angular/core';
import {MailListItem} from "./maillistitem";
import {MaillistService} from "./maillist.service";
import {SmartDatePipe} from "../../pipes/smart-date.pipe";
import {MaillistView} from "./maillistview";

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
  loading: boolean = false;
  currentView: MaillistView;

  pageSize = 50;
  totalSize: number;
  constructor(private maillistService: MaillistService) { }

  offset = 0;

  search(query) {
    // reset total size
    this.currentView = new MaillistView(query);
    this.totalSize = undefined;
    this.offset = 0;
    this.getMessages();
  }

  getMailsForAccount(accId) {
    // reset total size
    this.currentView = new MaillistView(null, accId);
    this.totalSize = undefined;
    this.offset = 0;
    this.getMessages();
  }

  getMailsForTagLineageId(tagLineageId) {
    // reset total size
    this.currentView = new MaillistView(null, null, null, tagLineageId);
    this.totalSize = undefined;
    this.offset = 0;
    this.getMessages();
  }

  getMailsByReadFlag(readFlag) {
    this.currentView = new MaillistView(null, null, null, null, readFlag);
    this.totalSize = undefined;
    this.offset = 0;
    this.getMessages();
  }

  getAllMails() {
    this.currentView = new MaillistView();
    this.totalSize = undefined;
    this.offset = 0;
    this.getMessages();
  }

  pagingfrom():number {
    return this.offset + 1;
  }

  pagingTo(): number {
    return Math.min(this.offset + this.pageSize, this.totalSize);
  }

  showFromTo(): boolean {
    // return typeof this.totalSize !== 'undefined';
    return this.totalSize > this.pageSize;
  }

  nextPage() {
    let newOffset = this.offset + this.pageSize;
    if(newOffset >= this.totalSize)
      return;
    this.offset = newOffset;
    this.getMessages();
  }

  prevPage() {
    let newOffset = this.offset - this.pageSize;
    if(newOffset < 0)
      return;
    this.offset = newOffset;
    this.getMessages();
  };

  firstPage() {
    this.offset = 0;
    this.getMessages();
  }

  lastPage() {
    let newOffset = this.totalSize - this.pageSize;
    if(newOffset < 0){
      newOffset = 0;
    }
    this.offset = newOffset;
    this.getMessages();
  }

  ngOnInit() {
    if(!this.currentView) {
      this.getAllMails();
    }
    else {
      this.getMessages();
    }
  }

  private getMessages() {
    this.loading = true;
    this.maillistService.getMessages(this.currentView, this.offset, this.pageSize)
      .subscribe(data => {
        this.mailListItems = data.messages;
        this.totalSize = data.totalCount;
      }, (error) => {
        console.log(error);
      }, () => {
        this.loading = false;
      });
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
