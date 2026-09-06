package vn.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Вынесен в отдельный бин, чтобы каждая попытка резерва шла через прокси
 * и выполнялась в собственной транзакции.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockReservationService {

    private final ProductStockRepository stockRepository;
    private final StockMapper stockMapper;

    @Transactional
    public StockCheckResponse reserve(ReserveRequest request) {
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
        return stockMapper.toReserveResponse(stock);
    }
}
