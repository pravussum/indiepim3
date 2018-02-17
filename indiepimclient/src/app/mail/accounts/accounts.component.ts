import {Component, OnInit} from '@angular/core';
import {AccountsService} from "./accounts.service";
import {AUTHENTICATION_TYPE, ENCRYPTION_TYPE, MessageAccount, SYNC_UPDATE_METHOD} from "./messageaccount";

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css'],
  providers: [AccountsService]
})

export class AccountsComponent implements OnInit {

  accounts : MessageAccount[] = [];
  selectedAccount: MessageAccount;

  encryptionTypes = ENCRYPTION_TYPE;
  authenticationTypes = AUTHENTICATION_TYPE;
  syncMethods = SYNC_UPDATE_METHOD;

  constructor(private accountsService: AccountsService) { }

  ngOnInit() {
      this.accountsService.getAccounts().subscribe(value => this.accounts = value);
  }

  onSelect(account: MessageAccount) {
    this.selectedAccount = account;
  }

  addAccount() {
    console.log("adding account");
    this.accounts.push({
      accountName : "New account",
      encryption: ENCRYPTION_TYPE.SSL,
      outgoingEncryption: ENCRYPTION_TYPE.SSL,
      authentication: AUTHENTICATION_TYPE.PASSWORD_NORMAL,
      outgoingAuthentication: AUTHENTICATION_TYPE.PASSWORD_NORMAL,
      syncMethod: SYNC_UPDATE_METHOD.FLAGS,
      syncInterval: 5
    });
  }

  removeAccount() {
    this.accountsService.removeAccount(this.selectedAccount).subscribe(result => console.log(result));
  }

  saveAccount() {
    this.accountsService.saveAccount(this.selectedAccount).subscribe(result => console.log(result));
  }
}
