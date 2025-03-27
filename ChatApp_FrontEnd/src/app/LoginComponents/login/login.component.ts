import { Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CheckLoginService } from '../../Services/CheckLogin/check-login.service';
import routerLinkList from '../../routerLinkList.json';
import { SharedUserInfoService } from '../../Services/shared/User/shared-user-info.service';
import { UserModel } from '../../Models/UserModel';
import { map, Observable } from 'rxjs';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink ,AsyncPipe],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  @ViewChild('formDir') formDir: any;
  loginForm! : FormGroup;

  afterGettedUserInfo$!: Observable<string>;

  routerLinkList = routerLinkList;

  constructor(private formBuilder: FormBuilder,
              private checkLoginService: CheckLoginService,
              private sharedUserInfoService: SharedUserInfoService,
              private router: Router)
  {
    this.loginForm = this.formBuilder.group({
      username: ['',Validators.required],
      password: ['',Validators.required],
      rememberMe: [false]
    });
  }

  get username() { return this.loginForm.get('username'); }

  get password() { return this.loginForm.get('password'); }

  resetLoginForm(): void {
    this.loginForm.reset();
    this.formDir.resetForm();
    this.afterGettedUserInfo$ = new Observable();
  }

  onSubmit(): void {
    console.log(this.loginForm.value);
    const formData = new FormData();
    formData.append('username', this.loginForm.value.username);
    formData.append('password', this.loginForm.value.password);
    formData.append('remember-me', this.loginForm.value.rememberMe);

    this.afterGettedUserInfo$ = this.checkLoginService.userLogin(formData).pipe(
      map(response => {
        if(response.status === "success"){
          console.log(response.message);
          //this.checkLoginService.setAuthToken(response.LoginToken);
          this.sharedUserInfoService.emitUserInfo(response.UserInfo as UserModel);
          this.router.navigate([routerLinkList[0].path]);
        }else{
          return response.msg;
        }
      })
    )
  }
}
