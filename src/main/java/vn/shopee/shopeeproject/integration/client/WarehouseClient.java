package vn.shopee.shopeeproject.integration.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vn.shopee.shopeeproject.config.warehouserestconfig.WarehouseConfig;
import vn.shopee.shopeeproject.exception.WarehouseUnavailableException;
import vn.shopee.shopeeproject.integration.dto.ReserveRequest;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

import java.util.UUID;

/**
 * Circuit breaker снаружи (аспект), retry внутри (RetryTemplate в теле метода),
 * поэтому fallback вызывается только после исчерпания попыток.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseClient {

    @Qualifier("warehouseRestTemplate")
    private final RestTemplate restTemplate;

    private final WarehouseConfig warehouseConfig;

    private final RetryTemplate retryTemplate;

    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackCheckStock")
    public StockCheckResponse checkStock(UUID productId, Integer quantity) {
        String url = UriComponentsBuilder.fromUriString(warehouseConfig.getUrl())
                .path("/api/warehouse/check")
                .queryParam("productId", productId)
                .queryParam("qty", quantity)
                .toUriString();

        return retryTemplate.execute(context -> {
            log.debug("Проверяем остаток на складе: productId={}, quantity={}, попытка={}",
                    productId, quantity, context.getRetryCount() + 1);
            return restTemplate.getForObject(url, StockCheckResponse.class);
        });
    }

    private StockCheckResponse fallbackCheckStock(UUID productId, Integer quantity, Throwable throwable) {
        throw unavailable("checkStock", productId, quantity, throwable);
    }

    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackReserveStock")
    public StockCheckResponse reserveStock(UUID productId, Integer quantity) {
        String url = UriComponentsBuilder.fromUriString(warehouseConfig.getUrl())
                .path("/api/warehouse/reserve")
                .toUriString();
        ReserveRequest body = ReserveRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();

        return retryTemplate.execute(context -> {
            log.debug("Резервируем на складе: productId={}, quantity={}, попытка={}",
                    productId, quantity, context.getRetryCount() + 1);
            return restTemplate.postForObject(url, body, StockCheckResponse.class);
        });
    }

    private StockCheckResponse fallbackReserveStock(UUID productId, Integer quantity, Throwable throwable) {
        throw unavailable("reserveStock", productId, quantity, throwable);
    }

    /**
     * Ответы 4xx — это отказ по бизнес-правилу склада, а не его недоступность,
     * поэтому они пробрасываются как есть и не превращаются в 503.
     */
    private WarehouseUnavailableException unavailable(String operation, UUID productId, Integer quantity,
                                                      Throwable throwable) {
        if (throwable instanceof HttpClientErrorException clientError) {
            throw clientError;
        }
        log.warn("Склад недоступен на {}: productId={}, quantity={}, причина={}",
                operation, productId, quantity, throwable.toString());
        return new WarehouseUnavailableException(
                "Склад недоступен на " + operation + ": productId=" + productId, throwable);
    }
}
