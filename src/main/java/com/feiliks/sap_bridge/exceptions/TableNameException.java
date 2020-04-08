package com.feiliks.sap_bridge.exceptions;


public class TableNameException extends ParameterException {
    public TableNameException() {
        super(1, "table name required");
    }
    public TableNameException(String table) {
        super(1, "table " + table + " not found");
    }
}
