import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor() {

    this.onsubmit();
  }

  ngOnInit(): void {
  }

  onError = (errorId: any, message: any) => {
    console.log("qr code load error", errorId, message)
  }


  onsubmit() {
    let nonce = Math.random().toString(36).substr(2, 5);
    let state = Math.random().toString(36).substr(2, 5);

    let authParamsSupplier = {
      state: state,
      nonce: nonce
    }

    const initAuthSessionResponse = NDI.initAuthSession(
      'ndi-qr',
      {
        clientId: 'xxx', //replace with your client id
        redirectUri: 'xxx', //replace with your redirect uri
        scope: 'openid',
        responseType: 'code'
      },
      authParamsSupplier,
      this.onError
    );
    console.log("initAuthSessionResponse:", initAuthSessionResponse)
  }

}
