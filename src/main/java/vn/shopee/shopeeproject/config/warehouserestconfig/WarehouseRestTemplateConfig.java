package vn.shopee.shopeeproject.config.warehouserestconfig;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import vn.shopee.shopeeproject.config.retry.IntegrationRetryProperties;

@Configuration
@RequiredArgsConstructor
public class WarehouseRestTemplateConfig {

    private final IntegrationRetryProperties properties;

    @Bean("warehouseRestTemplate")
    public RestTemplate warehouseRestTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(properties.getReadTimeout()))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(factory);
    }
}