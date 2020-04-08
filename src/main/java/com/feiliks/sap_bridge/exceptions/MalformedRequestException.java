package com.feiliks.sap_bridge.exceptions;


public class MalformedRequestException extends RequestException {
    public MalformedRequestException() {
        super(0, "malformed request data");
    }
}
