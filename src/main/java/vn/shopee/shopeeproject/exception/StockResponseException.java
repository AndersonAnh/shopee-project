package vn.shopee.shopeeproject.exception;

public class StockResponseException extends RuntimeException {
    public StockResponseException(String message) {
        super(message);
    }
}
