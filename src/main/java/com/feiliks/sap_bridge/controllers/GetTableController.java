package com.feiliks.sap_bridge.controllers;

import com.feiliks.sap_bridge.exceptions.FunctionNameException;
import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.exceptions.TableNameException;
import com.feiliks.sap_bridge.utils.JCoUtil;
import com.sap.conn.jco.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/get-table")
public class GetTableController extends AbstractSapBridgeController {
    private final static Logger LOG = LoggerFactory.getLogger(GetTableController.class);

    private static JCoFunction getFunction(JSONObject json) throws JCoException, FunctionNameException {
        try {
            return JCoUtil.getFunction(json.getString("function"));
        } catch (JSONException e) {
            throw new FunctionNameException();
        }
    }

    private static JCoTable getTable(JCoParameterList pList, JSONObject json) throws TableNameException {
        try {
            String name = json.getString("table");
            JCoTable t = pList.getTable(name);
            if (t == null)
                throw new TableNameException(name);
            return t;
        } catch (JSONException e) {
            throw new TableNameException();
        }
    }

    private static void readParams(JSONObject json, JCoParameterList out) {
        if (json != null) {
            for (String key : json.keySet())
                out.setValue(key, json.optString(key));
        }
    }

    private static List<String> json2StringList(JSONArray json) {
        List<String> out = new ArrayList<>(json.length());
        for (Object obj : json.toList()) {
            if (obj instanceof String)
                out.add((String) obj);
        }
        return out;
    }

    private static void readRecord(JCoFieldIterator iterator, JSONObject out) {
        while (iterator.hasNextField()) {
            JCoField field = iterator.nextField();
            out.put(field.getName(), field.getValue());
            /*
            switch (field.getType()) {
                case JCoMetaData.TYPE_STRING:
                    out.put(field.getName(), field.getString());
                    break;
            }
             */
        }
    }

    private static void readRecord(JCoTable table, List<String> fields, JSONObject out) {
        for (String field : fields)
            out.put(field, table.getValue(field));
    }

    private static JSONArray readRecords(JCoTable table, JSONArray fields) {
        JSONArray out = new JSONArray();
        if (fields == null) {
            for (int i = 0; i < table.getNumRows(); i++) {
                table.setRow(i);
                JSONObject row = new JSONObject();
                readRecord(table.getFieldIterator(), row);
                out.put(row);
            }
        } else {
            List<String> fieldList = json2StringList(fields);
            for (int i = 0; i < table.getNumRows(); i++) {
                table.setRow(i);
                JSONObject row = new JSONObject();
                readRecord(table, fieldList, row);
                out.put(row);
            }
        }
        return out;
    }

    @PostMapping
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long startTime = System.currentTimeMillis();
            JSONObject reqJson = readRequest(req);
            JCoFunction function = getFunction(reqJson);
            readParams(
                    reqJson.optJSONObject("parameters"),
                    function.getImportParameterList());
            function.execute(JCoUtil.getDestination());
            JCoTable table = getTable(function.getTableParameterList(), reqJson);
            JSONArray fields = reqJson.optJSONArray("fields");
            writeResult(resp, readRecords(table, fields));
            measureTime(reqJson.getString("function"), startTime);
        } catch (SapBridgeException e) {
            writeBadRequest(resp, e.getCode(), e.getMessage());
            logError(e);
        } catch (JCoException e) {
            LOG.error(e.getMessage(), e);
            writeError(resp, 300, e.getMessage());
            logException(e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            writeError(resp, 400, e.getMessage());
            logException(e);
        }
    }

}
