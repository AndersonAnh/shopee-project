package vn.shopee.shopeeproject.exception;

public class WarehouseUnavailableException extends RuntimeException {
    public WarehouseUnavailableException(String message) {
        super(message);
    }

    public WarehouseUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
