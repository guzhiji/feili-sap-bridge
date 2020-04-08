package com.feiliks.sap_bridge.exceptions;


public class SigVerificationException extends RequestException {
    public SigVerificationException() {
        super(2, "signature verification failed");
    }
}
