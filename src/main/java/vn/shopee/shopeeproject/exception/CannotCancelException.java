package vn.shopee.shopeeproject.exception;

import lombok.Getter;

@Getter
public class CannotCancelException extends RuntimeException {

  private final String currentStatus;

  public CannotCancelException(String message, String currentStatus) {
    super(message);
    this.currentStatus = currentStatus;
  }
}