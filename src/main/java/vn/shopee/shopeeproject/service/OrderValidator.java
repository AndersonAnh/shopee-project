package vn.shopee.shopeeproject.service;

import org.springframework.stereotype.Component;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.exception.CannotCancelException;

@Component
public class OrderValidator {

    public void canCancel(Order order) {
        OrderStatus status = order.getOrderStatus();
        if (status == OrderStatus.DELIVERED) {
            throw new CannotCancelException(
                    "Нельзя отменить доставленный заказ: " + order.getId(),
                    status.name());
        }
        if (status == OrderStatus.CANCELLED) {
            throw new CannotCancelException(
                    "Заказ уже отменён: " + order.getId(),
                    status.name());
        }
    }
}
