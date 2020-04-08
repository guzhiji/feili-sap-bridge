package com.feiliks.sap_bridge.exceptions;


public class ServiceNameException extends ParameterException {
    public ServiceNameException() {
        super(2, "service name required");
    }
    public ServiceNameException(String service) {
        super(2, "service " + service + " not found");
    }
}
