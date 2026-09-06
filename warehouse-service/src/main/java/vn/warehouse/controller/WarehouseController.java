package vn.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.warehouse.dto.ReserveRequest;
import vn.warehouse.dto.StockCheckResponse;
import vn.warehouse.service.WarehouseService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    public ResponseEntity<StockCheckResponse> checkStock(UUID productId, Integer quantity) {
        return ResponseEntity.ok(warehouseService.checkStock(productId, quantity));
    }

    @Override
    public ResponseEntity<StockCheckResponse> reserveStock(@Valid @RequestBody ReserveRequest request) {
        return ResponseEntity.ok(warehouseService.reserveStock(request));
    }
}