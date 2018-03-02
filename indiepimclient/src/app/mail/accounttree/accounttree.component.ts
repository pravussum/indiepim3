import {Component, OnInit, ViewChild} from '@angular/core';
import {AccountsService} from "../accounts/accounts.service";
import {MessageAccount} from "../accounts/messageaccount";
import {TreeComponent} from "angular-tree-component";
import {Router} from "@angular/router";

@Component({
  selector: 'app-accounttree',
  templateUrl: './accounttree.component.html',
  styleUrls: ['./accounttree.component.css'],
  providers: [AccountsService]
})
export class AccounttreeComponent implements OnInit {

  nodes = [];
  @ViewChild(TreeComponent)
  private tree: TreeComponent;

  constructor(private accountsService: AccountsService,
              private router: Router) { }

  ngOnInit() {
    this.accountsService.getAccounts().subscribe((accounts: MessageAccount[]) => {
        console.log("getAccounts returned");
        for (let account of accounts) {
          console.log("account " + account.accountName);
          console.dir(account.tagHierarchy);
          this.nodes.push(account.tagHierarchy);
        }
        this.tree.treeModel.update();
      }
    )
  }

  options = {};

  onActivate($event) {
    this.router.navigate((['frontend/maillist/taglineage/' + $event.node.id]));
  }
}
