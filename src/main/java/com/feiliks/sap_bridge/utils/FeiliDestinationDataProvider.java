package com.feiliks.sap_bridge.utils;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class FeiliDestinationDataProvider implements DestinationDataProvider {
    private final Map<String, Properties> properties = new HashMap<>();

    public FeiliDestinationDataProvider(String dest) throws IOException {
        Properties p = new Properties();
        p.load(getClass().getResourceAsStream("/sapjco.properties"));
        properties.put(dest, p);
    }

    public FeiliDestinationDataProvider(String configFile, String dest) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(configFile));
        properties.put(dest, p);
    }

    @Override
    public Properties getDestinationProperties(String s) throws DataProviderException {
        return properties.get(s);
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener destinationDataEventListener) {

    }
}
