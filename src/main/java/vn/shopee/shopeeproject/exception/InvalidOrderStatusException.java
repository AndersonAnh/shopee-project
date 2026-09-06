package vn.shopee.shopeeproject.exception;

public class InvalidOrderStatusException extends RuntimeException {
  public InvalidOrderStatusException(String message) {
    super(message);
  }
}
