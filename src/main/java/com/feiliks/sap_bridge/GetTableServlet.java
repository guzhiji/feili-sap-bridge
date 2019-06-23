package com.feiliks.sap_bridge;

import com.feiliks.sap_bridge.utils.StreamUtil;
import com.sap.conn.jco.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class GetTableServlet extends HttpServlet {

    private static class NoSignatureException extends SapBridgeException {
        NoSignatureException() {
            super(101, "signature required");
        }
    }

    private static class MalformedRequestException extends SapBridgeException {
        MalformedRequestException() {
            super(100, "malformed request data");
        }
    }

    private static class SigVerificationException extends SapBridgeException {
        SigVerificationException() {
            super(102, "signature verification failed");
        }
    }

    private static class FunctionNameException extends SapBridgeException {
        FunctionNameException() {
            super(200, "function name required");
        }
        FunctionNameException(String function) {
            super(200, "function " + function + " not found");
        }
    }

    private static class TableNameException extends SapBridgeException {
        TableNameException() {
            super(201, "table name required");
        }
        TableNameException(String table) {
            super(201, "table " + table + " not found");
        }
    }

    private final static String PASSWORD = "vK@mTfnjnyxy5iPD";
    private final static String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";
    private static JCoDestination jcoDestination = null;

    private static JCoDestination getDestination() throws JCoException {
        if (jcoDestination == null) {
            jcoDestination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
        }
        return jcoDestination;
    }

    private static JCoFunction getFunction(String name) throws JCoException, FunctionNameException {
        JCoFunction func = getDestination().getRepository().getFunction(name);
        if (func == null)
            throw new FunctionNameException(name);
        return func;
    }

    private static JCoFunction getFunction(JSONObject json) throws JCoException, FunctionNameException {
        try {
            return getFunction(json.getString("function"));
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

    private static void writeResult(HttpServletResponse resp, JSONArray result) throws IOException {
        JSONObject out = new JSONObject();
        out.put("code", 0);
        out.put("content", result);
        out.write(resp.getWriter());
    }

    private static void writeError(HttpServletResponse resp, int httpStatus, int code, String msg) throws IOException {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("content", msg);
        resp.setStatus(httpStatus);
        json.write(resp.getWriter());
    }

    private static void writeBadRequest(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_BAD_REQUEST, code, msg);
    }

    private static void writeError(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, code, msg);
    }

    private static String sign(String data) {
        return DigestUtils.sha256Hex(PASSWORD + '-' + data);
    }

    private static JSONObject readRequest(HttpServletRequest req) throws SapBridgeException {
        String signature = req.getHeader("X-SIG");
        if (signature == null) {
            signature = req.getParameter("sig");
            if (signature == null)
                throw new NoSignatureException();
        }
        try {
            String json = StreamUtil.readAsString(req.getInputStream(), StandardCharsets.UTF_8);
            if (!signature.equalsIgnoreCase(sign(json)))
                throw new SigVerificationException();
            return new JSONObject(json);
        } catch (JSONException | IOException e) {
            throw new MalformedRequestException();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JSONObject reqJson = readRequest(req);
            JCoFunction function = getFunction(reqJson);
            readParams(
                    reqJson.optJSONObject("parameters"),
                    function.getImportParameterList());
            function.execute(getDestination());
            JCoTable table = getTable(function.getTableParameterList(), reqJson);
            JSONArray fields = reqJson.optJSONArray("fields");
            writeResult(resp, readRecords(table, fields));
        } catch (SapBridgeException e) {
            writeBadRequest(resp, e.getCode(), e.getMessage());
        } catch (JCoException e) {
            writeError(resp, 300, e.getMessage());
        }
    }

}
