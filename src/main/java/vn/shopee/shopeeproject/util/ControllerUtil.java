package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.exception.NoSuchStatusException;

@UtilityClass
public class ControllerUtil {

    private final String CREATED = "CREATED";
    private final String ACCEPTED = "ACCEPTED";

    public ResponseEntity<OrderResponse> validatorResponse(OrderResponse orderResponse) {
        return
                switch (orderResponse.status()) {
                    case CREATED -> ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
                    case ACCEPTED -> ResponseEntity.status(HttpStatus.ACCEPTED).body(orderResponse);
                    default -> throw new NoSuchStatusException("Нет подходящего статуса: " + orderResponse.status());
                };
    }
}

