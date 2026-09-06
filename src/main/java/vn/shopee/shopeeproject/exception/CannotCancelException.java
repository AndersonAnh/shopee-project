package vn.shopee.shopeeproject.exception;

public class CannotCancelException extends RuntimeException {
  public CannotCancelException(String message) {
    super(message);
  }
}
