package com.feiliks.sap_bridge.controllers;

import com.feiliks.sap_bridge.exceptions.SapBridgeException;
import com.feiliks.sap_bridge.utils.JCoJson;
import com.feiliks.sap_bridge.utils.JCoUtil;
import com.sap.conn.jco.JCoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequestMapping("/api")
public class ApiBridgeController extends AbstractSapBridgeController {
    private final static Logger LOG = LoggerFactory.getLogger(ApiBridgeController.class);

    @PostMapping("/{func}")
    public void doPost(
            @PathVariable("func") String func,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        try {
            long startTime = System.currentTimeMillis();
            JCoJson jcoJson = new JCoJson(JCoUtil.getFunction(func));
            jcoJson.importParameters(readRequest(req));
            jcoJson.getFunction().execute(JCoUtil.getDestination());
            writeResult(resp, jcoJson.exportParameters());
            measureTime(func, startTime);
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
