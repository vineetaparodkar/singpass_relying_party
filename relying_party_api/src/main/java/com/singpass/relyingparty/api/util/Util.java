package com.singpass.relyingparty.api.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singpass.relyingparty.api.exception.SingpassException;
import com.singpass.relyingparty.api.model.JWKKeys;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class used for JWKS files
 */
public class Util {

    /**
     * Method to used to read JWKS file. Utility function used to read signing keys JSON files
     *
     * @param fileName Signing keys/JWKS file name
     * @return [JWKKeys] returns list of signing keys
     * @throws [SingpassException] thrown if file read operation fails
     */
    public List<JWKKeys> readJWKSFile(String fileName) throws SingpassException {
        try {
            // create object mapper instance
            ObjectMapper mapper = new ObjectMapper();

            InputStream inputStream = new FileInputStream(fileName);

            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            // convert JSON string to JWK object
            List<JWKKeys> jwks = Arrays.asList(mapper.readValue(inputStream, JWKKeys.class));

            return jwks;

        } catch (Exception ex) {
            throw new SingpassException("Failed to read jwks file" + ex);
        }
    }
}
