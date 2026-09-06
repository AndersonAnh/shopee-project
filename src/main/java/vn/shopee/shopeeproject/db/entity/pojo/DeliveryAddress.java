package vn.shopee.shopeeproject.pojo;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    private String city;
    private String street;
    private String house;
    private String apartment;
}
