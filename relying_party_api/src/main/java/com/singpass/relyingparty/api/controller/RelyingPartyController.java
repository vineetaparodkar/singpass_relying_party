package com.singpass.relyingparty.api.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.singpass.relyingparty.api.exception.SingpassException;
import com.singpass.relyingparty.api.service.SingpassService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;


/**
 * Controller class to receive Authentication Service Requests
 */
@RestController
@Api(tags = "Relying Party Controller")
public class RelyingPartyController {

    private static final Logger logger = LoggerFactory.getLogger(RelyingPartyController.class);

    @Autowired
    SingpassService singpassServiceInstance;

    @Value("${url.request.web.dashboard.uri}")
    private String webDashboardURL;

    @Value("${url.request.web.callback.redirect.uri}")
    private String webCallbackRedirectURI;


    /**
     * REST endpoint used by singpass for  redirect
     *
     * @param code  A 60-digit random string known as authorization code that is to be exchanged using out-of-band channel to fetch ID Token containing user details
     * @param state Same state parameter sent by relying party will be returned back to relying party for their validation
     * @return redirects to relying party's dashboard
     */
    @GetMapping(value = "/v0/auth/callback")
    @ApiOperation(value = "Web Redirect URL.", response = Void.class)
    @ApiResponses(value = {
            @ApiResponse(code = 302, message = "Found", response = Void.class),
            @ApiResponse(code = 500, message = "Internal Server Error. Try again.")
    })
    public ResponseEntity<Void> webRedirectURLEndpoint(@ApiParam(value = "Authorization code to be used for token endpoint") @RequestParam String code, @ApiParam(value = "Same state parameter sent by relying party ") @RequestParam String state) {

        logger.info("Started: mobileRedirectHandler controller.");

        logger.debug("Received code from Singpass:  " + code);
        logger.debug("Received state from Singpass:  " + state);

        try {
            String response = singpassServiceInstance.singpassCallbackHandler(code, webCallbackRedirectURI);
            logger.debug("Response from mobile callback handler:  " + response);

            String redirectURL = webDashboardURL + "?token=" + response;
            logger.debug("Redirect URL for mobile app:  " + redirectURL);

            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectURL)).build();

        } catch (Exception e) {
            logger.error("Exception occurred at Mobile redirect callback handler controller: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * REST endpoint used by singpass to fetch relying party's signing keys
     *
     * @return JWK set of relying party's signing keys
     */
    @GetMapping(value = "/v0/auth/jwks", produces = {"application/json", "application/xml"})
    @ApiOperation(value = "JWKS endpoint.", response = JWKSet.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success/OK", response = JWKSet.class)
    })
    public Map<String, Object> getJWKSEndpoint() {

        logger.info("Started: GetJWKSEndpoint controller.");
        try {
            JWKSet jwkSet = singpassServiceInstance.getJWKS();
            return jwkSet.toJSONObject();
        } catch (SingpassException e) {
            logger.info("Exception occurred in getJWKS Controller.", e);
            return null;
        }
    }

}
