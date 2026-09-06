package vn.shopee.shopeeproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    CREATED("Создан"),
    CONFIRMED("Одобрено"),
    WAITING_STOCK("Ожидается"),
    CANCELLED("Отказано");

    private String description;
}