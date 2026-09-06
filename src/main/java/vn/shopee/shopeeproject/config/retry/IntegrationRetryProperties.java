package vn.shopee.shopeeproject.config.retry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration.retry.config")
public class IntegrationRetryProperties {
    private int maxAttempts = 3;
    private long initialInterval = 2000L;
    private double multiplier = 2.0;
    private long maxInterval = 10000L;
    private List<String> retryableExceptions;
    private int connectTimeout = 2000;
    private int readTimeout = 5000;
}