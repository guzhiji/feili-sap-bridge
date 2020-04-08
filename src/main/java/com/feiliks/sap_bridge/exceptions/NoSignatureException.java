package com.feiliks.sap_bridge.exceptions;


public class NoSignatureException extends RequestException {
    public NoSignatureException() {
        super(1, "signature required");
    }
}
