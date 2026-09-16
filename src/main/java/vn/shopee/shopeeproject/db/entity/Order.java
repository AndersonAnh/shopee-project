package vn.shopee.shopeeproject.db.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.shopee.shopeeproject.db.entity.pojo.DeliveryAddress;
import vn.shopee.shopeeproject.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

    private LocalDate estimatedDelivery;

    private DeliveryAddress deliveryAddress;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
}