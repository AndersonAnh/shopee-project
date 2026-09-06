package vn.shopee.shopeeproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.shopee.shopeeproject.db.dto.OrderRequest;
import vn.shopee.shopeeproject.db.dto.OrderResponse;
import vn.shopee.shopeeproject.db.dto.PageResponse;
import vn.shopee.shopeeproject.db.dto.UpdateStatusRequest;

import java.util.UUID;

@RequestMapping("/api/orders")
public interface OrderApi {
    @PostMapping
    @Operation(description = "Метод отвечает за создание заказа")
    ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request
    );

    @GetMapping("/{id}")
    @Operation(description = "Получение заказа по ID")
    ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id
    );

    @GetMapping
    @Operation(description = "Получение списка заказов с пагинацией")
    ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    );

    @PatchMapping("/{id}/status")
    @Operation(description = "Обновление статуса заказа")
    ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request
    );

    @DeleteMapping("/{id}")
    @Operation(description = "Отмена заказа (soft delete)")
    ResponseEntity<Void> cancelOrder(
            @PathVariable UUID id
    );
}
