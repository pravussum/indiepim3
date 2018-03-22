import {Component, OnInit} from '@angular/core';
import {MessageAccount} from "../accounts/messageaccount";
import {AccountsService} from "../accounts/accounts.service";
import {SendService} from "./send.service";
import {SendMessage} from "./sendmessage";
import {EmailAddress} from "./emailaddress";
import {MailService} from "../mail.service";

@Component({
  selector: 'app-compose',
  templateUrl: './compose.component.html',
  styleUrls: ['./compose.component.css'],
  providers: [AccountsService, SendService, MailService]
})
export class ComposeComponent implements OnInit {

  accounts : Array<MessageAccount> = [];
  selectedAccount: MessageAccount;
  message = new SendMessage();
  addresses: Array<EmailAddress> = [];
  addressesStr: string[] = ["bernd"];
  autocmpl: true;
  autocompleteMustMatch: false;

  constructor(private accountsService: AccountsService,
              private sendService: SendService,
              private mailService: MailService) { }

  ngOnInit() {
    this.accountsService.getAccounts().subscribe(value =>  {
      this.accounts = value;
      if(this.accounts.length == 1) {
        this.selectedAccount = this.accounts[0];
        this.message.accountId = this.selectedAccount.id
        this.message.isHtml = true
        this.message.to = [];
      }
    },
      error => console.error(error)
    );

    this.mailService.getEmailAdresses().subscribe(
      data => {
        this.addresses = data;
        this.addressesStr = data.map(address => address.emailAddress);
        console.dir(this.addressesStr);
      },
      error => console.error(error)
      )
  }

  sendMessage() {
    console.log("sending message" + this.message.content);
    this.message.accountId = this.selectedAccount.id;
    this.message.isHtml = true;
    this.sendService.send(this.message).subscribe(result => console.log(result), error => console.log("error:" + error));
  }

  fileChange(event) {
    let fileList: FileList = event.target.files;
    console.dir(fileList);
  }
}
