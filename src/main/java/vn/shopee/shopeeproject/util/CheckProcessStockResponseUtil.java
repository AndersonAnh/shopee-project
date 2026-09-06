package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import vn.shopee.shopeeproject.exception.StockResponseException;
import vn.shopee.shopeeproject.integration.dto.StockCheckResponse;

@UtilityClass
@Slf4j
public class CheckProcessStockResponse {
    private static void processStockResponse(StockCheckResponse stockCheckResponse) {
        if (stockCheckResponse == null) {
            throw new StockResponseException("Сервис склада не доступен");
        }
    }
}
