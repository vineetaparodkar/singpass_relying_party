package com.singpass.relyingparty.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * JWKKeys class to store the JWK Keys.
 */
public class JWKKeys {

    @JsonProperty("keys")
    public ArrayList<JWK> keys;

}
