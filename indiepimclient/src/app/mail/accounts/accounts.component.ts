import {Component, OnInit} from '@angular/core';
import {AccountsService} from "./accounts.service";
import {AUTHENTICATION_TYPE, ENCRYPTION_TYPE, MessageAccount, SYNC_UPDATE_METHOD} from "./messageaccount";
import {LoginService} from "../../login/login.service";

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

  constructor(private accountsService: AccountsService, private loginService: LoginService) { }

  ngOnInit() {
      this.accountsService.getAccounts().subscribe(value => this.accounts = value);
      this.loginService.webSocketMsg$.subscribe(msg => this.onMessage(msg))
  }

  onSelect(account: MessageAccount) {
    this.selectedAccount = account;
  }

  addAccount() {
    this.accounts.push({
      accountName : "New account",
      encryption: ENCRYPTION_TYPE.SSL,
      outgoingEncryption: ENCRYPTION_TYPE.SSL,
      authentication: AUTHENTICATION_TYPE.PASSWORD_NORMAL,
      outgoingAuthentication: AUTHENTICATION_TYPE.PASSWORD_NORMAL,
      syncMethod: SYNC_UPDATE_METHOD.FLAGS,
      syncInterval: 5,
      trustInvalidSSLCertificates : false
    });
  }

  onMessage(msg: any) {
    if(msg.messageType == "AccountSyncProgress" || msg.messageType == "AccountSynced") {
      for(let account of this.accounts) {
        if(account.id == msg.accountId) {
          if(msg.messageType == "AccountSyncProgress") {
            account.syncActive = true;
            account.syncProgress = "Synchronizing folder " + msg.folder + "(" + msg.msgDoneCount + "/" + msg.msgCount + ")";
            account.syncProgressValue = msg.msgCount == 0 ? 0 : msg.msgDoneCount / msg.msgCount;
          } else if(msg.messageType == "AccountSynced") {
            account.syncActive = false
          }
        }
      }
    }
  }

  removeAccount() {
    this.accountsService.removeAccount(this.selectedAccount).subscribe(data => console.log(data), err => console.error(err));
  }

  saveAccount() {
    this.accountsService.saveAccount(this.selectedAccount).subscribe(data => console.log(data), err => console.error(err));
  }

  startAccountSync() {
    this.accountsService.startIncSync(this.selectedAccount).subscribe(data => console.log("sync finished."), err => console.error(err));
  }
}
