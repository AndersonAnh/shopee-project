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
import vn.warehouse.exception.InsufficientStockException;
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

    @Cacheable(value = "stockStatus", key = "#productId")
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(UUID productId, Integer quantity) {
        ProductStock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));
        return stockMapper.toCheckResponse(stock, quantity);
    }

    @Retryable(
            value = {OptimisticLockingFailureException.class}, // ретраим только транзиентные конфликты
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public StockCheckResponse reserveStock(ReserveRequest request) {
        return doReserveStock(request);
    }

    // Внутренний метод: своя транзакция на каждую попытку
    @Transactional
    public StockCheckResponse doReserveStock(ReserveRequest request) {
        UUID productId = request.productId();
        Integer quantity = request.quantity();

        ProductStock stock = stockRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));

        if (stock.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    String.format("Недостаточно товара. Доступно: %d, запрошено: %d",
                            stock.getAvailableQuantity(), quantity)
            );
        }

        stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
        stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
        stockRepository.save(stock);

        log.info("Зарезервировано {} единиц товара {}", quantity, productId);
        return stockMapper.toCheckResponse(stock, quantity);
    }
}