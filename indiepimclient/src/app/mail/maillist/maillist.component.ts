import { Component, OnInit } from '@angular/core';
import {MailListItem} from "./maillistitem";
import {MaillistService} from "./maillist.service";
import {SmartDatePipe} from "../../pipes/smart-date.pipe";
import {MaillistView} from "./maillistview";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";

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

  constructor(private maillistService: MaillistService,
              private router: Router,
              private route: ActivatedRoute) { }

  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
      this.updateCurrentViewFromRoute(params);
      this.getMessages();
    });
  }

  search(query) {
    this.currentView = new MaillistView(query);
    this.getMessages();
  }

  updateCurrentViewFromRoute(params: ParamMap) {
    if(!params) {
      this.currentView = new MaillistView();
      return;
    }
    if(params.get('query')) {
      this.currentView = new MaillistView(params.get('query'));
    } else if(params.get('accountid')) {
      this.currentView = new MaillistView(undefined, +params.get('accountid'));
    } else if(params.get('tagName')) {
      this.currentView = new MaillistView(undefined, undefined, params.get('tagName'));
    } else if(params.get('taglineageid')) {
      this.currentView = new MaillistView(undefined, undefined, undefined, +params.get('taglineageid'))
    } else if(params.get('readFlag')) {
      this.currentView = new MaillistView(undefined, undefined, undefined, undefined, !!params.get('readFlag'))
    } else {
      this.currentView = new MaillistView();
    }
  }


  private getMessages() {
    this.loading = true;
    this.maillistService.getMessages(this.currentView, this.pageSize)
      .subscribe(data => {
        this.mailListItems = data.messages;
        this.currentView.totalSize = data.totalCount;
      }, (error) => {
        console.log(error);
      }, () => {
        this.loading = false;
      });
  }

  viewSelectedMail(mailListItem: MailListItem) {
    this.router.navigate((['frontend/mailview/' + mailListItem.msgId]));
  }


  deleteMessage(mailListItem: MailListItem) {
    console.log("Deleting mail " + mailListItem.msgId)
  }

  toggleRead(mailListItem: MailListItem) {
    console.log("Toggle read " + mailListItem.msgId)
  }

  /*
   *
   *  Pagination stuff
   *
   */

  pagingfrom():number {
    return this.currentView.offset;
  }

  pagingTo(): number {
    return Math.min(this.currentView.offset + this.pageSize, this.currentView.totalSize);
  }

  showFromTo(): boolean {
    // return typeof this.totalSize !== 'undefined';
    return this.currentView.totalSize > this.pageSize;
  }

  nextPage() {
    let newOffset = this.currentView.offset + this.pageSize;
    if(newOffset >= this.currentView.totalSize)
      return;
    this.currentView.offset = newOffset;
    this.getMessages();
  }

  prevPage() {
    let newOffset = this.currentView.offset - this.pageSize;
    if(newOffset < 0)
      return;
    this.currentView.offset = newOffset;
    this.getMessages();
  };

  firstPage() {
    this.currentView.offset = 0;
    this.getMessages();
  }

  lastPage() {
    let newOffset = this.currentView.totalSize - this.pageSize;
    if(newOffset < 0){
      newOffset = 0;
    }
    this.currentView.offset = newOffset;
    this.getMessages();
  }
}
