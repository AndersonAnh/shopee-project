package vn.shopee.shopeeproject.integration.client;

import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import io.github.resilience4j.springboot3.spelresolver.autoconfigure.SpelResolverConfigurationOnMissingBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vn.shopee.shopeeproject.config.retry.IntegrationRetryProperties;
import vn.shopee.shopeeproject.config.retry.RetryConfig;
import vn.shopee.shopeeproject.config.warehouserestconfig.WarehouseConfig;
import vn.shopee.shopeeproject.exception.WarehouseUnavailableException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(
        classes = WarehouseClientResilienceTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("warehouse-client")
class WarehouseClientResilienceTest {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final int QUANTITY = 1;

    @Autowired
    private WarehouseClient warehouseClient;

    @Autowired
    private WarehouseConfig warehouseConfig;

    @Autowired
    private IntegrationRetryProperties retryProperties;

    @Autowired
    @Qualifier("warehouseRestTemplate")
    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void checkStock_whenWarehouseReturns5xx_retriesThenFailsAsUnavailable() {
        server.expect(times(retryProperties.getMaxAttempts()), requestTo(checkUrl()))
                .andRespond(withServerError());

        assertThatThrownBy(() -> warehouseClient.checkStock(PRODUCT_ID, QUANTITY))
                .isInstanceOf(WarehouseUnavailableException.class)
                .hasCauseInstanceOf(HttpServerErrorException.class);

        server.verify();
    }

    @Test
    void checkStock_whenWarehouseReturns4xx_propagatesClientError() {
        server.expect(times(1), requestTo(checkUrl()))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> warehouseClient.checkStock(PRODUCT_ID, QUANTITY))
                .isInstanceOf(HttpClientErrorException.NotFound.class);

        server.verify();
    }

    private String checkUrl() {
        return UriComponentsBuilder.fromUriString(warehouseConfig.getUrl())
                .path("/api/warehouse/check")
                .queryParam("productId", PRODUCT_ID)
                .queryParam("qty", QUANTITY)
                .toUriString();
    }

    @Configuration
    @EnableConfigurationProperties({IntegrationRetryProperties.class, WarehouseConfig.class})
    @Import({WarehouseClient.class, RetryConfig.class})
    @ImportAutoConfiguration({
            AopAutoConfiguration.class,
            ConfigurationPropertiesAutoConfiguration.class,
            SpelResolverConfigurationOnMissingBean.class,
            FallbackConfigurationOnMissingBean.class,
            CircuitBreakerAutoConfiguration.class
    })
    static class TestConfig {

        @Bean(name = "warehouseRestTemplate")
        RestTemplate warehouseRestTemplate() {
            return new RestTemplate();
        }
    }
}
