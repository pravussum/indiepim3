import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {LoginComponent} from './login/login.component';
import {AccountsComponent} from "./mail/accounts/accounts.component";
import {MaillistComponent} from "./mail/maillist/maillist.component";
import {MailviewComponent} from "./mail/mailview/mailview.component";
import {AccounttreeComponent} from "./mail/accounttree/accounttree.component";

const routes: Routes = [
  {path: 'frontend/login', component: LoginComponent},
  {path: '', component: MaillistComponent},
  {path: 'frontend/maillist', component: MaillistComponent},
  {path: 'frontend/maillist/taglineage/:taglineageid', component: MaillistComponent},
  {path: 'frontend/maillist/tag/:tagName', component: MaillistComponent},
  {path: 'frontend/maillist/account/:accountid', component: MaillistComponent},
  {path: 'frontend/maillist/search/:query', component: MaillistComponent},
  {path: 'frontend/maillist/search', component: MaillistComponent},
  {path: 'frontend/maillist/read/:readFlag', component: MaillistComponent},
  {path: 'frontend/mailview/:id', component: MailviewComponent},
  {path: 'frontend/mailaccounts', component: AccountsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
