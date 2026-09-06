package vn.shopee.shopeeproject.util;

import lombok.experimental.UtilityClass;
import vn.shopee.shopeeproject.db.dto.OrderItemRequest;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.exception.InvalidOrderException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CheckResponseUtil {

    public void validateCreateOrder(OrderRequest orderRequest) {
        List<String> errors = new ArrayList<>();

        if (orderRequest == null) {
            throw new InvalidOrderException(List.of("Тело запроса не может быть пустым"));
        }

        if (orderRequest.customerId() == null) {
            errors.add("customerId обязателен");
        }

        if (orderRequest.items() == null || orderRequest.items().isEmpty()) {
            errors.add("Заказ должен содержать хотя бы одну позицию");
        } else {
            for (OrderItemRequest item : orderRequest.items()) {
                validateOrderItem(item, errors);
            }
        }

        if (orderRequest.deliveryAddress() == null) {
            errors.add("Адрес доставки обязателен");
        }

        if (!errors.isEmpty()) {
            throw new InvalidOrderException(errors);
        }
    }

    private void validateOrderItem(OrderItemRequest item, List<String> errors) {
        if (item.quantity() == null || item.quantity() <= 0) {
            errors.add("Количество товара должно быть больше нуля: productId=" + item.productId());
        }
        if (item.price() == null || item.price().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Цена товара должна быть больше нуля: productId=" + item.productId());
        }
    }
}