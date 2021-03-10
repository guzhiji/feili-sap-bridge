package com.feiliks.sap_bridge.config;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.util.concurrent.TimeUnit;


@Configuration
public class JestConfig {

    @Autowired
    private JestProps jestProps;

    @Bean
    public JestClient jestClient() throws Exception {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(jestProps.getUris())
                .multiThreaded(true)
                .maxConnectionIdleTime(1, TimeUnit.MINUTES)
                .defaultMaxTotalConnectionPerRoute(2)
                .maxTotalConnection(10)
                .defaultCredentials(jestProps.getUsername(), jestProps.getPassword())
                .sslSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build());
        return factory.getObject();
    }
}
