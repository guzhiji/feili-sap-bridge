package com.feiliks.sap_bridge.controllers;

import com.feiliks.sap_bridge.exceptions.MalformedRequestException;
import com.feiliks.sap_bridge.exceptions.NoSignatureException;
import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.exceptions.SigVerificationException;
import com.feiliks.sap_bridge.utils.StreamUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public abstract class AbstractSapBridgeController {
    private final static String PASSWORD = "vK@mTfnjnyxy5iPD";

    private static String sign(String data) {
        return DigestUtils.sha256Hex(PASSWORD + '-' + data);
    }

    protected static JSONObject readRequest(HttpServletRequest req) throws SapBridgeException {
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

    protected static void writeResult(HttpServletResponse resp, JSONObject result) throws IOException {
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject out = new JSONObject();
        out.put("code", 0);
        out.put("content", result);
        out.write(resp.getWriter());
    }

    protected static void writeResult(HttpServletResponse resp, JSONArray result) throws IOException {
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
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
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
        json.write(resp.getWriter());
    }

    protected static void writeBadRequest(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_BAD_REQUEST, code, msg);
    }

    protected static void writeError(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, code, msg);
    }

}
