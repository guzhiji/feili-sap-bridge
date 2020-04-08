package com.feiliks.sap_bridge.exceptions;


public class FunctionNameException extends ParameterException {
    public FunctionNameException() {
        super(0, "function name required");
    }
    public FunctionNameException(String function) {
        super(0, "function " + function + " not found");
    }
}
