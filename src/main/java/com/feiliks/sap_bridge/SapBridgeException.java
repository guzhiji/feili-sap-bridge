package com.feiliks.sap_bridge;


public abstract class SapBridgeException extends Exception {

    private final int code;
    public SapBridgeException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
