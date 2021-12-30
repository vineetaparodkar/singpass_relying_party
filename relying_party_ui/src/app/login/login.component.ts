import { Component, OnInit } from '@angular/core';
import { SINGPASS_WEB_REDIRECT_URL, CLIENT_ID } from '../config/config'


/**
 *
 * Relying party login component which displays Singpass QR code to be scanned with singpass app
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor() {}

  ngOnInit(): void {
    this.onsubmit();
  }

  authParamsSupplier = () => {
    let nonce = Math.random().toString(36).substr(2, 5);
    let state = Math.random().toString(36).substr(2, 5);
    let param = {
      state: nonce,
      nonce: state
    };
    return param
  }

  onError = (errorId, message) => {
    console.log("qr code load error", errorId, message)
  }

  onsubmit() {
  
    const initAuthSessionResponse = NDI.initAuthSession(
      'ndi-qr',
      {
        clientId: CLIENT_ID,
        redirectUri: SINGPASS_WEB_REDIRECT_URL,
        scope: 'openid',
        responseType: 'code'
      },
      this.authParamsSupplier,
      this.onError
    );
    console.log("initAuthSessionResponse:", initAuthSessionResponse)
  }

}
