package vn.warehouse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.warehouse.dto.StockCheckResponse;
import vn.warehouse.entity.ProductStock;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(target = "productId", source = "stock.productId")
    @Mapping(target = "available", expression = "java(stock.getAvailableQuantity() >= quantity)")
    @Mapping(target = "availableQuantity", source = "stock.availableQuantity")
    @Mapping(target = "status", expression = "java(stock.getAvailableQuantity() >= quantity ? \"OK\" : \"INSUFFICIENT\")")
    StockCheckResponse toCheckResponse(ProductStock stock, Integer quantity);

    /**
     * Ответ на резерв: количество уже списано, поэтому available отражает факт
     * успешного резерва, а не сравнение остатка с запрошенным объёмом.
     */
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "availableQuantity", source = "availableQuantity")
    @Mapping(target = "status", constant = "RESERVED")
    StockCheckResponse toReserveResponse(ProductStock stock);
}
