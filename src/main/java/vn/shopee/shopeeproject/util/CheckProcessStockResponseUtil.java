package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vn.shopee.shopeeproject.exception.StockReserveResponseException;
import vn.shopee.shopeeproject.exception.StockResponseException;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

@UtilityClass
@Slf4j
public class CheckProcessStockResponseUtil {

    private static final String WAREHOUSE_UNAVAILABLE_STATUS = "CREATED";

    public static void processStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new StockResponseException("Сервис склада не доступен");
        }
        if (WAREHOUSE_UNAVAILABLE_STATUS.equals(stockCheckResponse.status())) {
            throw new StockResponseException("Сервис склада временно недоступен (сработал fallback)");
        }
        if (!stockCheckResponse.available()) {
            throw new StockResponseException("Товар отсутствует на складе: productId=" + stockCheckResponse.productId());
        }
    }

    public static void processReserveStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new StockReserveResponseException("Сервис склада не доступен для резервирования товара");
        }
        if (WAREHOUSE_UNAVAILABLE_STATUS.equals(stockCheckResponse.status())) {
            throw new StockReserveResponseException("Сервис склада временно недоступен при резервировании (сработал fallback)");
        }
        if (!stockCheckResponse.available()) {
            throw new StockReserveResponseException("Не удалось зарезервировать товар: productId=" + stockCheckResponse.productId());
        }
    }
}