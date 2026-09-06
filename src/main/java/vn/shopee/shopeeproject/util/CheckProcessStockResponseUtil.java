package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vn.shopee.shopeeproject.exception.StockReserveResponseException;
import vn.shopee.shopeeproject.exception.StockResponseException;
import vn.shopee.shopeeproject.exception.WarehouseUnavailableException;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

@UtilityClass
@Slf4j
public class CheckProcessStockResponseUtil {

    public static void processStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new WarehouseUnavailableException("Склад вернул пустой ответ на проверку остатка");
        }
        if (!stockCheckResponse.available()) {
            throw new StockResponseException("Товар отсутствует на складе: productId=" + stockCheckResponse.productId());
        }
    }

    public static void processReserveStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new WarehouseUnavailableException("Склад вернул пустой ответ на резервирование");
        }
        if (!stockCheckResponse.available()) {
            throw new StockReserveResponseException("Не удалось зарезервировать товар: productId=" + stockCheckResponse.productId());
        }
    }
}
