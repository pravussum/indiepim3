import { Component, OnInit } from '@angular/core';
import {MailviewService} from "./mailview.service";
import {ActivatedRoute, ParamMap} from "@angular/router";
import {MailItem} from "./mailItem";

@Component({
  selector: 'app-mailview',
  templateUrl: './mailview.component.html',
  styleUrls: ['./mailview.component.css'],
  providers: [MailviewService]
})
export class MailviewComponent implements OnInit {

  constructor(private mailViewService : MailviewService, private route: ActivatedRoute) { }

  currentMail : MailItem;
  mailContentUrl : string;

  ngOnInit() {
    this.route.paramMap.subscribe((params) => {
       this.updateViewFromParams(params);
    });
  }

  updateViewFromParams(params: ParamMap) {
    this.mailViewService.getMessage(+params.get('id'))
      .subscribe((mail) => {
        this.currentMail = mail;
        this.createMailContentBlob();
      });
  }

  private createMailContentBlob() {
    let content: string;
    if (this.currentMail.contentHtml) {
      content = this.currentMail.contentHtml;
    } else if (this.currentMail.contentText) {
      content = "<pre>" + this.currentMail.contentText + "</pre>";
    } else {
      content = "The message contained no valid content.";
    }
    let blob = new Blob([content], {type: "text/html"});
    this.mailContentUrl = URL.createObjectURL(blob);
  }

  reply() {
    console.log("replying to message " + this.currentMail.id)
  }

  replyAll() {
    console.log("replying all to message " + this.currentMail.id)
  }

  forward() {
    console.log("forwarding message " + this.currentMail.id)
  }

  delete() {
    console.log("deleting message " + this.currentMail.id)
  }
}
