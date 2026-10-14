package vn.warehouse.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import vn.warehouse.dto.ErrorResponseDto;
import vn.warehouse.exception.InsufficientStockException;
import vn.warehouse.exception.ProductNotFoundException;

@ResponseBody
@Slf4j
@ControllerAdvice
public class WarehouseExceptionHandler {


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductNotFoundException.class)
    protected ErrorResponseDto handleNotFound(ProductNotFoundException ex) {
        log.warn(ex.getMessage(), ex);
        return ErrorResponseDto.builder()
                .code("NOT_FOUND")
                .message("Продукт не найден")
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InsufficientStockException.class)
    protected ErrorResponseDto handleInsufficientStock(InsufficientStockException ex) {
        log.warn(ex.getMessage(), ex);
        return ErrorResponseDto.builder()
                .code("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .build();
    }
}
