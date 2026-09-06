package vn.warehouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.warehouse.dto.ReserveRequest;
import vn.warehouse.dto.StockCheckResponse;

import java.util.UUID;

@RequestMapping("/api/warehouse")
public interface WarehouseApi {

    @Operation(summary = "Проверка наличия товара")
    @GetMapping("/check")
    ResponseEntity<StockCheckResponse> checkStock(
            @Parameter(description = "Идентификатор товара") @RequestParam UUID productId,
            @Parameter(description = "Запрашиваемое количество") @RequestParam("qty") Integer quantity
    );

    @Operation(summary = "Резервирование товара")
    @PostMapping("/reserve")
    ResponseEntity<StockCheckResponse> reserveStock(
            @Valid @RequestBody ReserveRequest request
    );
}
