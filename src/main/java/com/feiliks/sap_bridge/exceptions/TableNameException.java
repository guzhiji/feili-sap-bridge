package com.feiliks.sap_bridge.exceptions;

public class TableNameException extends SapBridgeException {
    public TableNameException() {
        super(201, "table name required");
    }
    public TableNameException(String table) {
        super(201, "table " + table + " not found");
    }
}
