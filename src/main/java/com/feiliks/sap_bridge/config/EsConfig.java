package com.feiliks.sap_bridge.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;


@Configuration
public class EsConfig {

    @Autowired
    private EsProps esProps;

    @Bean
    public RestClientBuilder restClientBuilder() {
        HttpHost[] hosts = esProps.getUris().stream()
                .map(host -> {
                    try {
                        URI uri = new URI(host);
                        return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
                    } catch (URISyntaxException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(HttpHost[]::new);
        return RestClient.builder(hosts).setHttpClientConfigCallback(builder -> {
            try {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                        esProps.getUsername(), esProps.getPassword()));
                SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(new TrustAllStrategy())
                        .build();
                return builder.setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            } catch (Throwable th) {
                throw new RuntimeException(th);
            }
        });
    }

    @Bean
    public RestHighLevelClient highLevelClient(RestClientBuilder restClientBuilder) {
        return new RestHighLevelClient(restClientBuilder);
    }

}