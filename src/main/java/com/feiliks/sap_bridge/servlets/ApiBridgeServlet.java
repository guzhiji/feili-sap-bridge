package com.feiliks.sap_bridge.servlets;

import com.feiliks.sap_bridge.exceptions.FunctionNameException;
import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.utils.JCoJson;
import com.feiliks.sap_bridge.utils.JCoUtil;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ApiBridgeServlet extends AbstractSapBridgeServlet {
    private final static Pattern PATH_PT = Pattern.compile("/([^/]+)");

    private JCoFunction getFunction(String pathInfo) throws FunctionNameException, JCoException {
        if (pathInfo == null)
            throw new FunctionNameException();
        Matcher matcher = PATH_PT.matcher(pathInfo);
        if (!matcher.find())
            throw new FunctionNameException();
        return JCoUtil.getFunction(matcher.group(1));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JCoJson jcoJson = new JCoJson(getFunction(req.getPathInfo()));
            jcoJson.importParameters(readRequest(req));
            jcoJson.getFunction().execute(JCoUtil.getDestination());
            JSONObject result = jcoJson.getTableParameters();
            if (result == null)
                result = jcoJson.getExportParameters();
            if (result == null)
                result = jcoJson.getChangingParameters();
            writeResult(resp, result);
        } catch (SapBridgeException e) {
            writeBadRequest(resp, e.getCode(), e.getMessage());
        } catch (JCoException e) {
            writeError(resp, 300, e.getMessage());
        } catch (Exception e) {
            writeError(resp, 400, e.getMessage());
        }
    }

}
