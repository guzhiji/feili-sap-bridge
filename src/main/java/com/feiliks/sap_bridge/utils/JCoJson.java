package com.feiliks.sap_bridge.utils;

import com.sap.conn.jco.*;
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

    public void importParameters(JSONObject json) {
        setStructure(func.getImportParameterList(), json);
    }

    public JSONObject getExportParameters() {
        return getObject(func.getExportParameterList());
    }

    public JSONObject getTableParameters() {
        return getObject(func.getTableParameterList());
    }

    private void setStructure(JCoRecord output, JSONObject data) {
        for (String key : data.keySet()) {
            Object val = data.opt(key);
            if (val == null || JSONObject.NULL.equals(val))
                continue;
            if (val instanceof JSONObject)
                setStructure(output.getStructure(key), (JSONObject) val);
            else if (val instanceof JSONArray)
                setTable(output.getTable(key), (JSONArray) val);
            else
                output.setValue(key, val);
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
