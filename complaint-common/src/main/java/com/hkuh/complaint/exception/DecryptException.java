package com.hkuh.complaint.exception;

public class DecryptException extends RuntimeException {

    public DecryptException() {
        super();
    }

    public DecryptException(String message) {
        super(message);
    }

    public DecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecryptException(Throwable cause) {
        super(cause);
    }
}
