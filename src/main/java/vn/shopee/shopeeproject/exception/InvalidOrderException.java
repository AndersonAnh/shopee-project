package vn.shopee.shopeeproject.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidOrderException extends RuntimeException {

    private final List<String> details;

    public InvalidOrderException(String message) {
        super(message);
        this.details = List.of(message);
    }

    public InvalidOrderException(List<String> details) {
        super(String.join("; ", details));
        this.details = details;
    }
}