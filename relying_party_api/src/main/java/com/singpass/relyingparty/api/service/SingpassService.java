package com.singpass.relyingparty.api.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.singpass.relyingparty.api.exception.SingpassException;

/**
 * interface for Relying party
 */
public interface SingpassService {

    /**
     * Creates Singpass Request to be sent to SINGPASS OIDC to fetch ID token
     *
     * @param authCode    Authentication code returned by singpass in OIDC flow
     * @param redirectURI relying party's redirect URI used for singpass Auth session initialization
     * @return [idToken] a JWT ID token
     * @throws [SingpassException] thrown if fetching of singpass ID token fails
     */
    String singpassCallbackHandler(String authCode, String redirectURI) throws SingpassException;

    /**
     * Method to handle JWKS request.Returns relying party's signing keys in JWKS format
     *
     * @return [JWK Set] JWK set of relying party's signing keys
     * @throws [SingpassException] thrown if file read operation fails or if keys cannot be converted to JWKS format
     */
    JWKSet getJWKS() throws SingpassException;

}
