package vn.shopee.shopeeproject.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;
import vn.shopee.shopeeproject.dto.ErrorDto;
import vn.shopee.shopeeproject.exception.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {


    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    protected ErrorDto handleOrderNotFound(OrderNotFoundException exception) {
        log.warn("Заказ не найден: {}", exception.getMessage(), exception);
        return ErrorDto.builder()
                .code("ORDER_NOT_FOUND")
                .header("Заказ не найден")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        log.warn("Ошибка валидации запроса: {}", details);
        return ErrorDto.builder()
                .code("INVALID_ORDER")
                .header("Некорректные данные заказа")
                .message(details.toString())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidOrderException.class)
    protected ErrorDto handleInvalidOrder(InvalidOrderException exception) {
        log.warn("Невалидный заказ: {}", exception.getDetails());
        return ErrorDto.builder()
                .code("INVALID_ORDER")
                .header("Некорректные данные заказа")
                .message(exception.getDetails() != null ? exception.getDetails().toString() : exception.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoSuchStatusException.class)
    protected ErrorDto handleNoSuchStatus(NoSuchStatusException exception) {
        log.warn("Некорректный статус заказа: {}", exception.getMessage());
        return ErrorDto.builder()
                .code("NO_SUCH_STATUS")
                .header("Некорректный статус")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidOrderStatusException.class)
    protected ErrorDto handleInvalidOrderStatus(InvalidOrderStatusException exception) {
        log.warn("Недопустимый переход статуса: {}", exception.getMessage());
        return ErrorDto.builder()
                .code("INVALID_ORDER_STATUS")
                .header("Недопустимый статус заказа")
                .message(exception.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CannotCancelException.class)
    protected ErrorDto handleCannotCancel(CannotCancelException exception) {
        log.warn("Невозможно отменить заказ: текущий статус {}", exception.getCurrentStatus(), exception);
        return ErrorDto.builder()
                .code("CANNOT_CANCEL")
                .header("Отмена заказа невозможна")
                .message("Заказ в статусе «" + exception.getCurrentStatus() + "» не может быть отменён.")
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(WarehouseUnavailableException.class)
    protected ErrorDto handleWarehouseUnavailable(WarehouseUnavailableException exception) {
        log.error("Склад недоступен (retry исчерпан): {}", exception.getMessage(), exception);
        return ErrorDto.builder()
                .code("WAREHOUSE_UNAVAILABLE")
                .header("Склад временно недоступен")
                .message("Не удалось подтвердить наличие товара на складе. Попробуйте позже.")
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler({
            SocketTimeoutException.class,
            ResourceAccessException.class,
            IOException.class
    })
    protected ErrorDto handleIntegrationException(Exception exception) {
        log.error("Ошибка интеграции с внешним сервисом: {}", exception.getMessage(), exception);
        return ErrorDto.builder()
                .code("INTEGRATION_ERROR")
                .header("Внешний сервис недоступен")
                .message("Не удалось выполнить операцию из-за проблем с внешним сервисом. Пожалуйста, повторите попытку позже.")
                .build();
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    protected ErrorDto handleGeneric(Exception exception) {
        log.error("Непредвиденная ошибка: {}", exception.getMessage(), exception);
        return ErrorDto.builder()
                .code("INTERNAL_ERROR")
                .header("Внутренняя ошибка сервера")
                .message("Произошла непредвиденная ошибка. Пожалуйста, обратитесь в службу поддержки.")
                .build();
    }
}