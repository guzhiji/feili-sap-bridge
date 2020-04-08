package com.feiliks.sap_bridge.exceptions;


abstract class RequestException extends SapBridgeException {
    RequestException(int codeOffset, String msg) {
        super(100 + codeOffset, msg);
    }
}
