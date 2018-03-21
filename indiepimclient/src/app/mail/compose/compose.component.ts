import { Component, OnInit } from '@angular/core';
import {MessageAccount} from "../accounts/messageaccount";
import {AccountsService} from "../accounts/accounts.service";
import {SendService} from "./send.service";
import {SendMessage} from "./sendmessage";

@Component({
  selector: 'app-compose',
  templateUrl: './compose.component.html',
  styleUrls: ['./compose.component.css'],
  providers: [AccountsService, SendService]
})
export class ComposeComponent implements OnInit {

  accounts : MessageAccount[] = [];
  selectedAccount: MessageAccount;
  message = new SendMessage();

  constructor(private accountsService: AccountsService,
              private sendService: SendService) { }

  ngOnInit() {
    this.accountsService.getAccounts().subscribe(value =>  {
      this.accounts = value;
      if(this.accounts.length == 1) {
        this.selectedAccount = this.accounts[0];
        this.message.accountId = this.selectedAccount.id
        this.message.isHtml = true
      }
    });

  }

  sendMessage() {
    console.log("sending message" + this.message.content);
    this.message.accountId = this.selectedAccount.id;
    this.message.isHtml = true;
    this.sendService.send(this.message).subscribe(result => console.log(result), error => console.log("error:" + error));
  }
}
