package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.exception.NoSuchStatusException;

@UtilityClass
public class ControllerUtil {

    public ResponseEntity<OrderResponse> validatorResponse(OrderResponse orderResponse) {
        OrderStatus status = parse(orderResponse.status());
        return switch (status) {
            case CONFIRMED, CREATED -> ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
            case WAITING_STOCK -> ResponseEntity.status(HttpStatus.ACCEPTED).body(orderResponse);
            default -> throw new NoSuchStatusException("Нет подходящего статуса: " + orderResponse.status());
        };
    }

    private OrderStatus parse(String status) {
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new NoSuchStatusException("Нет подходящего статуса: " + status);
        }
    }
}
