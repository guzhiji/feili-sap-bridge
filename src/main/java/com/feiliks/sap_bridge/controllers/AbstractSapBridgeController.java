package com.feiliks.sap_bridge.controllers;

import com.feiliks.sap_bridge.exceptions.MalformedRequestException;
import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.utils.RequestContextUtil;
import com.feiliks.sap_bridge.utils.StreamUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


abstract class AbstractSapBridgeController {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractSapBridgeController.class);
    private final static String INDEX_PREFIX = "sap-bridge-";
    private final static ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMMdd"));
    private final static ThreadLocal<DateFormat> DATETIME_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

    @Autowired
    private JestClient jestClient;

    private void fillHostInfo(Map<String, Object> doc) {
        HttpServletRequest req = RequestContextUtil.getRequest();
        if (req != null) {
            doc.put("host", req.getServerName());
            doc.put("port", req.getServerPort());
        }
    }

    protected void measureTime(String func, long start) {
        try {
            Date now = new Date();
            Map<String, Object> doc = new HashMap<>();
            doc.put("@timestamp", DATETIME_FORMAT.get().format(now));
            fillHostInfo(doc);
            doc.put("java_class", getClass().getCanonicalName());
            doc.put("sap_function", func);
            doc.put("execution_time", System.currentTimeMillis() - start);
            Index index = new Index.Builder(doc)
                    .index(INDEX_PREFIX + "measure-" + DATE_FORMAT.get().format(now))
                    .type("measure")
                    .build();
            jestClient.executeAsync(index, null);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    protected void logError(SapBridgeException ex) {
        try {
            Date now = new Date();
            Map<String, Object> doc = new HashMap<>();
            doc.put("@timestamp", DATETIME_FORMAT.get().format(now));
            fillHostInfo(doc);
            doc.put("java_class", getClass().getCanonicalName());
            doc.put("message", ex.toString());
            doc.put("type", ex.getClass().getName());
            doc.put("error_code", ex.getCode());
            Index index = new Index.Builder(doc)
                    .index(INDEX_PREFIX + "error-" + DATE_FORMAT.get().format(now))
                    .type("error")
                    .build();
            jestClient.executeAsync(index, null);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    protected void logException(Throwable throwable) {
        try {
            Date now = new Date();
            Map<String, Object> doc = new HashMap<>();
            doc.put("@timestamp", DATETIME_FORMAT.get().format(now));
            fillHostInfo(doc);
            doc.put("java_class", getClass().getCanonicalName());
            doc.put("message", throwable.toString());
            doc.put("type", throwable.getClass().getCanonicalName());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            doc.put("stack-trace", sw.toString());
            Index index = new Index.Builder(doc)
                    .index(INDEX_PREFIX + "exception-" + DATE_FORMAT.get().format(now))
                    .type("exception")
                    .build();
            jestClient.executeAsync(index, null);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    /*
    @Value("${sap-bridge.password}")
    private String PASSWORD;

    private String sign(String data) {
        return DigestUtils.sha256Hex(PASSWORD + '-' + data);
    }
     */

    protected JSONObject readRequest(HttpServletRequest req) throws SapBridgeException, IOException {
        /*
        String signature = req.getHeader("X-SIG");
        if (signature == null) {
            signature = req.getParameter("sig");
            if (signature == null)
                throw new NoSignatureException();
        }
         */
        try {
            String json = StreamUtil.readAsString(req.getInputStream(), StandardCharsets.UTF_8);
            /*
            if (!signature.equalsIgnoreCase(sign(json)))
                throw new SigVerificationException();
             */
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new MalformedRequestException();
        }
    }

    protected void writeResult(HttpServletResponse resp, JSONObject result) throws IOException {
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject out = new JSONObject();
        out.put("code", 0);
        out.put("content", result);
        out.write(resp.getWriter());
    }

    protected void writeResult(HttpServletResponse resp, JSONArray result) throws IOException {
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject out = new JSONObject();
        out.put("code", 0);
        out.put("content", result);
        out.write(resp.getWriter());
    }

    private void writeError(HttpServletResponse resp, int httpStatus, int code, String msg) throws IOException {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("content", msg);
        resp.setStatus(httpStatus);
        resp.setHeader("Content-Type", "application/json; charset=UTF-8");
        json.write(resp.getWriter());
    }

    protected void writeBadRequest(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_BAD_REQUEST, code, msg);
    }

    protected void writeError(HttpServletResponse resp, int code, String msg) throws IOException {
        writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, code, msg);
    }

}
