package vn.shopee.shopeeproject.integration.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import vn.shopee.shopeeproject.config.warehouserestconfig.WarehouseConfig;
import vn.shopee.shopeeproject.integration.dto.ReserveRequest;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseClient {

    @Qualifier("warehouseRestTemplate")
    private final RestTemplate restTemplate;

    private final WarehouseConfig warehouseConfig;

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            noRetryFor = {CallNotPermittedException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackCheckStock")
    public StockCheckResponse checkStock(UUID productId, Integer quantity) {
        log.debug("Проверяем остаток на складе: productId={}, quantity={}", productId, quantity);
        String url = UriComponentsBuilder.fromUriString(warehouseConfig.getWarehouseUrl())
                .path("/api/warehouse/check")
                .queryParam("productId", productId)
                .queryParam("qty", quantity)
                .toUriString();
        return restTemplate.getForObject(url, StockCheckResponse.class);
    }

    private StockCheckResponse fallbackCheckStock(UUID productId, Integer quantity, Throwable throwable) {
        log.warn("Fallback сработал для checkStock: productId={}, quantity={}, причина={}",
                productId, quantity, throwable.toString());

        return StockCheckResponse.builder()
                .productId(productId)
                .available(false)
                .availableQuantity(0)
                .status("CREATED")
                .build();
    }

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            noRetryFor = {CallNotPermittedException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackReserveStock")
    public StockCheckResponse reserveStock(UUID productId, Integer quantity) {
        log.debug("Резервируем на складе: productId={}, quantity={}", productId, quantity);
        String url = UriComponentsBuilder.fromUriString(warehouseConfig.getWarehouseUrl())
                .path("/api/warehouse/reserve")
                .toUriString();
        ReserveRequest body = ReserveRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .build();
        return restTemplate.postForObject(url, body, StockCheckResponse.class);
    }

    private StockCheckResponse fallbackReserveStock(UUID productId, Integer quantity, Throwable throwable) {
        log.warn("Fallback сработал для reserveStock: productId={}, quantity={}, причина={}",
                productId, quantity, throwable.toString());

        return StockCheckResponse.builder()
                .productId(productId)
                .available(false)
                .availableQuantity(0)
                .status("CREATED")
                .build();
    }
}
