package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import vn.shopee.shopeeproject.exception.StockReserveResponseException;
import vn.shopee.shopeeproject.exception.WarehouseUnavailableException;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

@UtilityClass
public class CheckProcessStockResponseUtil {

    public static void processStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new WarehouseUnavailableException("Склад вернул пустой ответ на проверку остатка");
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

    public static void processReleaseStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new WarehouseUnavailableException("Склад вернул пустой ответ на освобождение резерва");
        }
        if (!stockCheckResponse.available()) {
            throw new StockReserveResponseException("Не удалось освободить резерв: productId=" + stockCheckResponse.productId());
        }
    }
}
