package com.singpass.relyingparty.api.exception;

/**
 * Class used to catch all Singpass Exceptions
 */
public class SingpassException extends Exception {

    public SingpassException(String message, Exception e) {
        super(message, e);
    }

    public SingpassException(String message) {
        super(message);
    }
}
