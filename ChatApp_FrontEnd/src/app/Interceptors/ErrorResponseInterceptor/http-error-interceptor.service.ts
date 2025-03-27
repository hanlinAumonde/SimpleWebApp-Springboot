import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import properties from '../../properties.json';
import routerLinkList from '../../routerLinkList.json';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class HttpErrorInterceptorService implements HttpInterceptor{
    constructor(private router:Router) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                let errorMessage:string = 'e';
                if(error.status === 401){
                    errorMessage = 'Error: Unauthorized';
                }
                else if(req.url.includes(properties.ChatroomApi.BaseUrl) && req.method === 'POST' && error.status === 409){
                    errorMessage = 'Error: Chatroom already exists';
                    return throwError(() => this.handleError(errorMessage, 1));
                }
                else if(req.url.includes(properties.ChatroomApi.BaseUrl) && req.method === 'DELETE' && (error.status === 404 || error.status === 409 || error.status === 500)){
                    errorMessage = "Erreur lors de l'operation de la Chatroom";
                    return throwError(() => this.handleError(errorMessage, 2));
                }
                else if(req.url.includes(properties.ChatroomApi.BaseUrl) && req.method === 'PUT' && error.status === 409){
                    errorMessage = "Erreur lors de l'operation de la Chatroom";
                    let chatroomId = parseInt(req.url.split(properties.ChatroomApi.BaseUrl)[1]);
                    return throwError(() => this.handleError(errorMessage, 4, chatroomId));
                }
                else if(req.url.includes(properties.ChatroomApi.BaseUrl) && (req.method === 'GET' || req.method === 'PUT') && error.status === 403){
                    errorMessage = "Error: vous n'avez pas l'autorisation d'acceder a cette ressource";
                    return throwError(() => this.handleError(errorMessage, 0));
                }
                else{
                    errorMessage = 'Error: Cannot connect to the server';
                }
                return throwError(() => this.handleError(errorMessage, -1));
            })
        );
    }

    handleError(errorMsg: string, pathId: number, chatroomId?: number){
        if(pathId === -1){
            console.log(errorMsg);
            this.router.navigate([routerLinkList[6].path]);
        }else{
            alert(errorMsg);
            if(chatroomId){
                this.router.navigateByUrl('/', {skipLocationChange: true}).then(() => {
                    this.router.navigate([routerLinkList[pathId].path + chatroomId]);
                });
            }else{
                this.router.navigateByUrl('/', {skipLocationChange: true}).then(() => {
                    this.router.navigate([routerLinkList[pathId].path]);
                });
            }
        }
    }
}
