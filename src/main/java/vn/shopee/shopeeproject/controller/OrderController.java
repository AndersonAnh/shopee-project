package vn.shopee.shopeeproject.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.dto.PageResponse;
import vn.shopee.shopeeproject.db.dto.UpdateStatusRequest;
import vn.shopee.shopeeproject.service.OrderService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {
    private final OrderService orderService;


    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrderById(UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Override
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(orderService.getOrders(pageable)));
    }

    @Override
    public ResponseEntity<OrderResponse> updateStatus(UUID id, UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @Override
    public ResponseEntity<Void> cancelOrder(UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
