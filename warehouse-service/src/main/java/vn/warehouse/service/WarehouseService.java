package vn.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.warehouse.dto.ReserveRequest;
import vn.warehouse.dto.StockCheckResponse;
import vn.warehouse.entity.ProductStock;
import vn.warehouse.exception.ProductNotFoundException;
import vn.warehouse.mapper.StockMapper;
import vn.warehouse.repository.ProductStockRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final ProductStockRepository stockRepository;
    private final StockMapper stockMapper;
    private final StockReservationService stockReservationService;

    @Cacheable(value = "stockStatus", key = "{#productId, #quantity}")
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(UUID productId, Integer quantity) {
        ProductStock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));
        return stockMapper.toCheckResponse(stock, quantity);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class}, // ретраим только транзиентные конфликты
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public StockCheckResponse reserveStock(ReserveRequest request) {
        return stockReservationService.reserve(request);
    }

    @Retryable(
            retryFor = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public StockCheckResponse releaseStock(ReserveRequest request) {
        return stockReservationService.release(request);
    }
}
