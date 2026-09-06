package vn.shopee.shopeeproject.integration.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import vn.shopee.shopeeproject.config.warehouserestconfig.WarehouseConfig;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WarehouseClient {

    @Qualifier("warehouseRestTemplate")
    private final RestTemplate restTemplate;

    private final WarehouseConfig warehouseConfig;

    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackCheckStock")
    public StockCheckResponse checkStock(UUID productId, Integer quantity) {
        log.debug("Проверяем остаток на складе: productId={}, quantity={}", productId, quantity);
        return restTemplate.getForObject(
                warehouseConfig.getWarehouseUrl() + "/stock/" + productId + "?quantity=" + quantity,
                StockCheckResponse.class
        );
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

    @CircuitBreaker(name = "warehouse", fallbackMethod = "fallbackReserveStock")
    public StockCheckResponse reserveStock(UUID productId, Integer quantity) {
        log.debug("Резервируем на складе: productId={}, quantity={}", productId, quantity);
        return restTemplate.getForObject(
                warehouseConfig.getWarehouseUrl() + "/stock/" + productId + "?quantity=" + quantity,
                StockCheckResponse.class
        );
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