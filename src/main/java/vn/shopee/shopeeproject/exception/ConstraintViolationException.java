package vn.shopee.shopeeproject.exception;

public class ConstraintViolationException extends RuntimeException {
  public ConstraintViolationException(String message) {
    super(message);
  }
}
