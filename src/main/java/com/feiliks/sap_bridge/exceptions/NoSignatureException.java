package com.feiliks.sap_bridge.exceptions;

public class NoSignatureException extends SapBridgeException {
    public NoSignatureException() {
        super(101, "signature required");
    }
}
