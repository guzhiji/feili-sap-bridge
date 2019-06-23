package com.feiliks.sap_bridge.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


public class StreamUtil {

    public static final int BUFFER_SIZE = 4096;

    public static String readAsString(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }

    public static String readAsString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] block = new byte[BUFFER_SIZE];
        int r;
        do {
            r = is.read(block);
            if (r > 0) baos.write(block, 0, r);
        } while (r > -1);
        return baos.toString();
    }

}
