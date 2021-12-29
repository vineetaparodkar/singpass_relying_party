package com.singpass.relyingparty.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JWK class to store the JWK metadata.
 */
public class JWK {
    @JsonProperty("kty")
    String kty;

    @JsonProperty("d")
    String d;

    @JsonProperty("use")
    String use;

    @JsonProperty("crv")
    String crv;

    @JsonProperty("kid")
    String kid;

    @JsonProperty("x")
    String x;

    @JsonProperty("y")
    String y;

    @JsonProperty("alg")
    String alg;

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getCrv() {
        return crv;
    }

    public void setCrv(String crv) {
        this.crv = crv;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
