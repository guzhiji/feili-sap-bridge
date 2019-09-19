package com.feiliks.sap_bridge;

import com.feiliks.sap_bridge.utils.JCoUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class BootApplication {

    public static void main(String[] args) throws IOException {
        JCoUtil.init();
        SpringApplication.run(BootApplication.class, args);
    }

}
