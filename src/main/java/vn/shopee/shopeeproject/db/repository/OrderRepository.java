package vn.shopee.shopeeproject.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.shopee.shopeeproject.db.entity.Order;
import vn.shopee.shopeeproject.enums.OrderStatus;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByOrderStatusNot(OrderStatus status, Pageable pageable);
}