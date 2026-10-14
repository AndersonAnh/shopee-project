package vn.shopee.shopeeproject.exception;

public class ConnectTimeoutException extends RuntimeException {
    public ConnectTimeoutException(String message) {
        super(message);
    }
}
