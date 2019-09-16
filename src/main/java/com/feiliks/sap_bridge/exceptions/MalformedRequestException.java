package com.feiliks.sap_bridge.exceptions;

public class MalformedRequestException extends SapBridgeException {
    public MalformedRequestException() {
        super(100, "malformed request data");
    }
}
