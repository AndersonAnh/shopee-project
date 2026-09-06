package vn.shopee.shopeeproject.db.entity.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DeliveryAddress {

    @NotBlank(message = "city обязателен")
    @Column(name = "city")
    private String city;

    @NotBlank(message = "street обязателен")
    @Column(name = "street")
    private String street;

    @NotBlank(message = "house обязателен")
    @Column(name = "house")
    private String house;

    @Column(name = "apartment")
    private String apartment;
}