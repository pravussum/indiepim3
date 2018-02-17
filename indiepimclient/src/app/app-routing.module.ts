import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {LoginComponent} from './login/login.component';
import {AccountsComponent} from "./mail/accounts/accounts.component";
import {MaillistComponent} from "./mail/maillist/maillist.component";

const routes: Routes = [
   {path: 'frontend/login', component: LoginComponent},
   {path: '', component: MaillistComponent},
   {path: 'frontend/maillist', component: MaillistComponent},
   {path: 'frontend/mailaccounts', component: AccountsComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
