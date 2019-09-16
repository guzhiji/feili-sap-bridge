package com.feiliks.sap_bridge.utils;

import com.feiliks.sap_bridge.exceptions.FunctionNameException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.ext.Environment;

import java.io.IOException;


public class JCoUtil {

    private final static String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";
    private static JCoDestination jcoDestination = null;
    static {
        try {
            Environment.registerDestinationDataProvider(new FeiliDestinationDataProvider(
                    ABAP_AS_POOLED));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JCoDestination getDestination() throws JCoException {
        if (jcoDestination == null) {
            jcoDestination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
        }
        return jcoDestination;
    }

    public static JCoFunction getFunction(String name) throws JCoException, FunctionNameException {
        JCoFunction func = getDestination().getRepository().getFunction(name);
        if (func == null)
            throw new FunctionNameException(name);
        return func;
    }

}
