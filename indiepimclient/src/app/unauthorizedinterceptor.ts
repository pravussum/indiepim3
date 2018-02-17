import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/do'
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";

@Injectable()
export class UnauthorizedInterceptor implements HttpInterceptor {

  constructor(private router : Router) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).do((event : HttpEvent<any>) => {
      //console.log(event);
    },  (err: any) => {
      if(err.status === 401) {
        console.log("Unauthorized");
        localStorage.removeItem('currentUser');
        this.router.navigate((['/frontend/login']));
      }
    });

  }

}
