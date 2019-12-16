package com.feiliks.sap_bridge.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


public class RequestContextUtil {

    public static HttpServletRequest getRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null)
            return null;
        ServletRequestAttributes sAttrs = (ServletRequestAttributes) attrs;
        return sAttrs.getRequest();
    }
}
