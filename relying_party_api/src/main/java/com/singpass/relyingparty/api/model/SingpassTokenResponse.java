package com.singpass.relyingparty.api.model;

/**
 * JWK class to store the singpass token response.
 */
public class SingpassTokenResponse {

    String access_token;
    String token_type;
    String id_token;

    public String getId_token() {
        return id_token;
    }

    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }
}
