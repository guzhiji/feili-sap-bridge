package com.feiliks.sap_bridge.utils;

import com.sap.conn.jco.*;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;


public class JCoJson {

    private final JCoFunction func;

    public JCoJson(JCoFunction func) {
        this.func = func;
    }

    public JCoFunction getFunction() {
        return func;
    }

    private void readParams(JCoParameterList params, JSONObject json) {
        if (params != null) {
            JSONObject data = new JSONObject();
            JCoListMetaData meta = params.getListMetaData();
            for (int i = 0; i < meta.getFieldCount(); i++) {
                String name = meta.getName(i);
                if (json.has(name))
                    data.put(name, json.get(name));
            }
            setStructure(params, data);
        }
    }

    public void importParameters(JSONObject json) {
        readParams(func.getImportParameterList(), json);
        readParams(func.getTableParameterList(), json);
    }

    private JSONObject exportParams(JCoParameterList params) {
        if (params == null)
            return null;
        return getObject(params);
    }

    public JSONObject getExportParameters() {
        return exportParams(func.getExportParameterList());
    }

    public JSONObject getTableParameters() {
        return exportParams(func.getTableParameterList());
    }

    public JSONObject getChangingParameters() {
        return exportParams(func.getChangingParameterList());
    }

    private void setStructure(JCoRecord output, JSONObject data) {
        for (String key : data.keySet()) {
            Object val = data.opt(key);
            if (val == null || JSONObject.NULL.equals(val))
                continue;
            if (val instanceof JSONObject) {
                setStructure(output.getStructure(key), (JSONObject) val);
            } else if (val instanceof JSONArray) {
                setTable(output.getTable(key), (JSONArray) val);
            } else {
                if (val instanceof String &&
                        output.getMetaData().getType(key) == JCoMetaData.TYPE_BYTE) {
                    output.setValue(key, Base64.decodeBase64((String) val));
                } else {
                    output.setValue(key, val);
                }
            }
        }
    }

    private void setTable(JCoTable output, JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.optJSONObject(i);
            if (row == null)
                continue;
            output.appendRow();
            setStructure(output, row);
        }
    }

    private JSONObject getObject(JCoRecord data) {
        JSONObject output = new JSONObject();
        JCoFieldIterator it = data.getFieldIterator();
        while (it.hasNextField()) {
            JCoField field = it.nextField();
            if (field.isStructure()) {
                output.put(field.getName(), getObject(field.getStructure()));
            } else if (field.isTable()) {
                output.put(field.getName(), getArray(field.getTable()));
            } else if (field.getType() == JCoMetaData.TYPE_BYTE) {
                byte[] bytes = field.getByteArray();
                if (bytes != null)
                    output.put(field.getName(), Base64.encodeBase64String(bytes));
            } else {
                output.put(field.getName(), field.getValue());
            }
        }
        return output;
    }

    private JSONArray getArray(JCoTable data) {
        JSONArray output = new JSONArray();
        if (!data.isEmpty()) {
            data.firstRow();
            do {
                output.put(getObject(data));
            } while (data.nextRow());
        }
        return output;
    }

}
