package vn.shopee.shopeeproject.service;

import org.junit.jupiter.api.Test;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.enums.OrderStatus;
import vn.shopee.shopeeproject.exception.CannotCancelException;
import vn.shopee.shopeeproject.util.TestUtil;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidatorTest {

    private final OrderValidator orderValidator = new OrderValidator();

    @Test
    void canCancel_rejectsDeliveredOrder() {
        Order order = TestUtil.getOrder();
        order.setOrderStatus(OrderStatus.DELIVERED);

        assertThrows(CannotCancelException.class, () -> orderValidator.canCancel(order));
    }

    @Test
    void canCancel_rejectsAlreadyCancelledOrder() {
        Order order = TestUtil.getOrder();
        order.setOrderStatus(OrderStatus.CANCELLED);

        assertThrows(CannotCancelException.class, () -> orderValidator.canCancel(order));
    }

    @Test
    void canCancel_allowsConfirmedOrder() {
        Order order = TestUtil.getOrder();
        order.setOrderStatus(OrderStatus.CONFIRMED);

        assertDoesNotThrow(() -> orderValidator.canCancel(order));
    }
}
