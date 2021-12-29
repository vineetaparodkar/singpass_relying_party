package com.singpass.relyingparty.api.service;


import com.singpass.relyingparty.api.model.JWK;
import com.singpass.relyingparty.api.model.JWKKeys;
import com.singpass.relyingparty.api.util.Util;
import com.singpass.relyingparty.api.model.SingpassTokenResponse;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.singpass.relyingparty.api.exception.SingpassException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service Class to handle authentication requests
 */
@Service("singpassService")
public class SingpassServiceImpl implements SingpassService {

    private static final Logger logger = LoggerFactory.getLogger(SingpassServiceImpl.class);


    @Value("${url.request.singpassToken}")
    private String singpassAccessTokenUrl;

    @Value("${url.request.clientId}")
    private String clientID;

    @Value("${url.request.grantType}")
    private String grantType;

    @Value("${url.request.jwksUri}")
    private String jwksUrl;

    @Value("${url.request.audience}")
    private String audience;

    @Value("${url.request.signingKeyID}")
    private String signingKeyID;

    @Autowired
    private RestTemplate restTemplate;

    private URI tokenURI;

    private JSONObject singpassJWKS;


    @PostConstruct
    public void init() throws URISyntaxException, MalformedURLException {

        tokenURI = new URI(singpassAccessTokenUrl);

    }

    /**
     * Method to create Singpass Request to be sent to SINGPASS OIDC to fetch ID token
     *
     * @param authCode    Authentication code returned by singpass in OIDC flow
     * @param redirectURI relying party's redirect URI used for singpass Auth session initialization
     * @return [idToken] a JWT ID token
     * @throws [SingpassException] thrown if fetching of singpass ID token fails and session token signing fails
     */
    @Override
    public String singpassCallbackHandler(String authCode, String redirectURI) throws SingpassException {

        try {

            List<JWKKeys> signingKeyList = new Util().readJWKSFile("signingKey.json");

            //Loading JWKS
            JSONObject signingKeyJSON = new JSONObject();
            JWK jwk = signingKeyList.get(0).keys.get(0);

            //Reading signing key from file
            signingKeyJSON.put("kty", jwk.getKty());
            signingKeyJSON.put("d", jwk.getD());
            signingKeyJSON.put("use", jwk.getUse());
            signingKeyJSON.put("crv", jwk.getCrv());
            signingKeyJSON.put("kid", jwk.getKid());
            signingKeyJSON.put("x", jwk.getX());
            signingKeyJSON.put("y", jwk.getY());
            signingKeyJSON.put("alg", jwk.getAlg());

            logger.debug("JWKS, Signing key json" + signingKeyJSON);

            //Initialising Response variables
            ResponseEntity<SingpassTokenResponse> accessTokenResponse = null;
            ResponseEntity<String> jwksResponse = null;


            // assemble headers for Token API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setCacheControl("private, no-store, max-age=0");
            logger.debug("Token API Request Headers" + headers);

            //creating client assertion
            JWSHeader clientAssertionHeader = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .type(JOSEObjectType.JWT)
                    .keyID(signingKeyID).build();

            logger.debug("Client Assertion Headers" + clientAssertionHeader);

            ECKey privateKey = ECKey.parse(signingKeyJSON.toString());
            logger.debug("Signing key private key" + privateKey);

            JWSSigner signer = new ECDSASigner(privateKey.toECPrivateKey());
            logger.debug("Client Assertion Signer" + signer);
            JWTClaimsSet payload = new JWTClaimsSet.Builder()
                    .issuer(clientID)
                    .audience(audience)
                    .subject(clientID)
                    .issueTime(new Date())
                    .expirationTime(new Date((new Date()).getTime() + 120000)) //hardcoding 120000 as request fails if expiration time is more than 2 min
                    .build();

            logger.debug("Payload for Client Assertion" + payload.toJSONObject());

            SignedJWT signedJWT = new SignedJWT(clientAssertionHeader, payload);
            logger.debug("Signed Client Assertion JWT" + signedJWT);
            signedJWT.sign(signer);
            logger.debug("Signed Client Assertion JWT" + signedJWT);

            String clientAssertion = signedJWT.serialize();
            logger.debug("Serialized client assertion" + clientAssertion);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("client_id", clientID);
            formData.add("code", authCode);
            formData.add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
            formData.add("client_assertion", clientAssertion);
            formData.add("grant_type", grantType);
            formData.add("redirect_uri", redirectURI);

            logger.debug("Form data for token endpoint" + formData);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            accessTokenResponse = restTemplate.postForEntity(tokenURI, entity, SingpassTokenResponse.class);

            logger.debug("Received access token response" + accessTokenResponse.getBody().getAccess_token());
            logger.debug("Received ID_Token" + accessTokenResponse.getBody().getId_token());

            //Verify signature on Access-token
            //Decrypting token
            JWEObject encryptedJWT = JWEObject.parse(accessTokenResponse.getBody().getId_token());
            logger.debug("Encrypted JWT Token Received from token endpoint" + encryptedJWT);

            //Loading encryption private key
            JSONObject encryptedJsonKey = new JSONObject();

            //Reading encryption key from file
            JWK encryptedjwk = signingKeyList.get(0).keys.get(1);

            encryptedJsonKey.put("kty", encryptedjwk.getKty());
            encryptedJsonKey.put("d", encryptedjwk.getD());
            encryptedJsonKey.put("use", encryptedjwk.getUse());
            encryptedJsonKey.put("crv", encryptedjwk.getCrv());
            encryptedJsonKey.put("kid", encryptedjwk.getKid());
            encryptedJsonKey.put("x", encryptedjwk.getX());
            encryptedJsonKey.put("y", encryptedjwk.getY());
            encryptedJsonKey.put("alg", encryptedjwk.getAlg());

            ECKey encryptionPrivateKey = ECKey.parse(encryptedJsonKey.toString());


            ECDHDecrypter decrypter = new ECDHDecrypter(encryptionPrivateKey.toECPrivateKey());
            logger.debug("Decrypter" + decrypter);
            encryptedJWT.decrypt(decrypter);
            logger.debug("Encrypted JWT" + encryptedJWT);
            Payload payloadDecrypted = encryptedJWT.getPayload();
            logger.debug("Decrypted Payload" + payloadDecrypted);

            URI singpassJwksURL = new URI(jwksUrl);


            // assemble headers for Singpass JWKS API
            HttpHeaders jwksHeader = new HttpHeaders();
            jwksHeader.add("Accept", "*/*");
            logger.debug("SINGPASS JWKS Headers" + jwksHeader);

            ResponseEntity<String> singpassJWKSResponse = restTemplate.getForEntity(singpassJwksURL, String.class);

            logger.debug("Singpass  JWKS response" + singpassJWKSResponse.getBody());

            JSONObject singpassJWKSParameters = new JSONObject(singpassJWKSResponse.getBody());
            logger.debug("singpassJWKSParameters JSONARRAY" + (JSONArray) singpassJWKSParameters.get("keys"));
            singpassJWKSParameters = ((JSONArray) singpassJWKSParameters.get("keys")).getJSONObject(0);


            //Loading public key
            singpassJWKS = new JSONObject();
            singpassJWKS.put("kty", singpassJWKSParameters.getString("kty"));
            singpassJWKS.put("use", singpassJWKSParameters.getString("use"));
            singpassJWKS.put("crv", singpassJWKSParameters.getString("crv"));
            singpassJWKS.put("kid", singpassJWKSParameters.getString("kid"));
            singpassJWKS.put("x", singpassJWKSParameters.getString("x"));
            singpassJWKS.put("y", singpassJWKSParameters.getString("y"));
            singpassJWKS.put("alg", "ES256"); //hardcoding here as alg is not sent by singpass in JWKS endpoint


            //JWKS for singpass
            ECKey singpassPublicKey = ECKey.parse(this.singpassJWKS.toString());
            SignedJWT decryptedJWT = payloadDecrypted.toSignedJWT();
            logger.debug("Decrypted JWT" + decryptedJWT);
            JWSVerifier jwsVerifier = new ECDSAVerifier(singpassPublicKey.toECPublicKey());
            logger.debug("JWSVerifier" + jwsVerifier);
            decryptedJWT.verify(jwsVerifier);
            logger.debug("DONE validation success" + decryptedJWT);
            //Validation succeded

            // Extract payload
            SignedJWT signedPayload = encryptedJWT.getPayload().toSignedJWT();
            String JWTSubject = signedPayload.getJWTClaimsSet().getSubject();
            logger.debug("decryptedJWT: " + decryptedJWT);
            Map<String, String> reconstructedSubjectMap = Arrays.stream(JWTSubject.split(","))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));

            logger.debug("ReconstructedSubjectMap.get(u)" + reconstructedSubjectMap.get("u"));

            logger.debug("Session Token Received" + reconstructedSubjectMap.get("s"));
            String idToken=reconstructedSubjectMap.get("s");
            return idToken;

        } catch (Exception e) {
            logger.error("Exception Occurred in callback handler service " + e);
            throw new SingpassException("Exception occurred while calling Singpass redirect Service", e);
        }

    }

    /**
     * Method to handle JWKS request.Returns relying party's signing keys in JWKS format
     *
     * @return [JWK Set] JWK set of relying party's signing keys
     * @throws [SingpassException] thrown if file read operation fails or if keys cannot be converted to JWKS format
     */
    @Override
    public JWKSet getJWKS() throws SingpassException {
        logger.info("GetJWKS  service started");
        try {

            InputStream inputStream = new FileInputStream("jwks.json");
            File tempFile = File.createTempFile("temp", ".txt");
            try {
                FileUtils.copyInputStreamToFile(inputStream, tempFile);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            return JWKSet.load(tempFile);
        } catch (Exception e) {
            throw new SingpassException("Exception occurred while calling Singpass getJWKS Service", e);
        }

    }
}
