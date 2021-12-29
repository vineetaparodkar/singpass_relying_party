# Relying Party API

This file contains instructions to build,
configure and install Relying Party API to demonstrate relying party backend integration with Singpass Login API.


## Pre-requisites

- [Java 8](https://www.oracle.com/java/technologies/downloads/) 

- [SSL certificate](https://letsencrypt.org/) 

- Keystore (Refer Appendix section) 


## Initial Setup

1. Clone the repository and checkout to main branch


## API Setup

1. Application properties and its usage are described below.

    **Application Property** | **Description** 
    ------------------------ | ----------------- 
    server.port |  Relying party server HTTPS port.
    server.host | Relying party server host.
    server.ssl.key-store | The path to the truststore file. 
    server.ssl.key-store-type | Standard keystore type used.
    server.ssl.key-store-password | The password of the keystore.
    server.ssl.key-alias | The alias (or name) under which the key is stored in the keystore.
    url.request.singpassToken |  Singpass's token endpoint URI used to fetch token ID.
    url.request.grant_type | The type of grant being requested for client assertion.
    url.request.jwks_uri | Singpass's JWKS endpoint url.
    url.request.audience | The recipient of client assertion. `e.g. https://id.singpass.gov.sg` 
    url.request.client_id | The clientId that was pre-registered with Singpass during onboarding
    url.request.signingKeyID | Signing key ID of signing key used for signing client assertion
    url.request.web.callback.redirect.uri | The web URL that Singpass will redirect the user to after the user authenticates with the Singpass app
    url.request.web.dashboard.uri | Dashboard URL for web wallet to which user will be redirected to on completing OIDC flow 
    logging.level.root | Log levels severity mapping. For instance, `logging.level.root=DEBUG`.
    
### **Local Deployment**

a. Update application properties from resources folder here, `relying_party_api/src/main/resources`. Also place your JWKS in jwks.json file (This file should only contain signing and encryption public key set in JWKS format) and place your signing and public key JWKS in signingKey.json file(This file should contain both enryption and signing public and private key in JWKS format)
               
b. Create deployment folders according to instructions given below.

1. Create a relying party deployment folder.

2. Copy below files from `relying_party_api/src/main/resources` to relying party's deployment folder.

    - `relying_party_api/src/main/resources/application.properties`
    - `relying_party_api/src/main/resources/jwks.json`
    - `relying_party_api/src/main/resources/signingKey.json`
 
c. Execute following command from project root directory to create relying party's service jar.

   `./gradlew build -x test`
        
d. New jar will be created here `relying_party_api/build/libs/relying_party_api-0.0.1-SNAPSHOT.jar`. Copy this same jar in relying party's deployment folder.

e. Execute below command to start relying party's API.
- Navigate to relying party's deployment folder and execute below command.
        
    `java -jar relying_party_api-0.0.1-SNAPSHOT.jar`

            
## Swagger UI for API Specification.

- Use below URL to access Swagger UI.   

    `https://<servername>:<port>/swagger-ui.html`        

## Appendix
- Use below command to generate keystore.   

    `openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out keystore.p12 -name tomcat  -CAfile chain.pem  -caname root`

- Check if the SSL certificate chain from your origin server is complete.To check use this.   
  
    `https://www.ssllabs.com/ssltest/` 