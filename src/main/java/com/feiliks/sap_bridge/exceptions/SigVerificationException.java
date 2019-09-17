package com.feiliks.sap_bridge.exceptions;

public class SigVerificationException extends SapBridgeException {
    public SigVerificationException() {
        super(102, "signature verification failed");
    }
}
