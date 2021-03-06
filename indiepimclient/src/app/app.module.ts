import {BrowserModule} from '@angular/platform-browser';
import {Injectable, NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HTTP_INTERCEPTORS, HttpClientModule, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {AppRoutingModule} from './app-routing.module';

import {AppComponent} from './app.component';
import {LoginComponent} from './login/login.component';
import {RouterModule} from "@angular/router";
import {AccountsComponent} from './mail/accounts/accounts.component';
import {UnauthorizedInterceptor} from "./unauthorizedinterceptor";
import {MaillistComponent} from './mail/maillist/maillist.component';
import {NavpanelComponent} from './navpanel/navpanel.component';
import {LoginService} from "./login/login.service";
import {KeysPipe} from "./pipes/keys.pipe";
import {RelativeDatePipe} from "./pipes/relative-date.pipe";
import {SmartDatePipe} from "./pipes/smart-date.pipe";
import { MailviewComponent } from './mail/mailview/mailview.component';
import {SafePipe} from "./pipes/safe.pipe";
import { AccounttreeComponent } from './mail/accounttree/accounttree.component';
import {TreeModule} from "angular-tree-component";
import { ComposeComponent } from './mail/compose/compose.component';
import {CKEditorModule} from 'ng2-ckeditor';
import {RlTagInputModule} from 'angular2-tag-input';
import { UsersettingsComponent } from './user/usersettings/usersettings.component';

@Injectable()
export class XhrInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const xhr = req.clone({
      headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
    });
    return next.handle(xhr);
  }
}

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AccountsComponent,
    MaillistComponent,
    NavpanelComponent,
    KeysPipe,
    RelativeDatePipe,
    SmartDatePipe,
    SafePipe,
    MailviewComponent,
    AccounttreeComponent,
    ComposeComponent,
    UsersettingsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    RouterModule,
    TreeModule,
    CKEditorModule,
    RlTagInputModule
  ],
  providers: [
    LoginService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: XhrInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: UnauthorizedInterceptor,
      multi: true
    }
  ],
  exports: [
    KeysPipe,
    RelativeDatePipe,
    SmartDatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }


