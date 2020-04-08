package com.feiliks.sap_bridge.exceptions;


abstract class ParameterException extends SapBridgeException {
    ParameterException(int codeOffset, String msg) {
        super(200 + codeOffset, msg);
    }
}
