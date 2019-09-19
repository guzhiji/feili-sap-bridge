package com.feiliks.sap_bridge.controllers;

import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.utils.JCoJson;
import com.feiliks.sap_bridge.utils.JCoUtil;
import com.sap.conn.jco.JCoException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequestMapping("/api")
public class ApiBridgeServlet extends AbstractSapBridgeController {

    @PostMapping("/{func}")
    public void doPost(
            @PathVariable("func") String func,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        try {
            JCoJson jcoJson = new JCoJson(JCoUtil.getFunction(func));
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
