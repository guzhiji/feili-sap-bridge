package com.feiliks.sap_bridge.exceptions;

public class FunctionNameException extends SapBridgeException {
    public FunctionNameException() {
        super(200, "function name required");
    }
    public FunctionNameException(String function) {
        super(200, "function " + function + " not found");
    }
}
