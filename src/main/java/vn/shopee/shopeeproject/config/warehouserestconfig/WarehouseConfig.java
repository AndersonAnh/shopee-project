package vn.shopee.shopeeproject.config.warehouserestconfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("integration.warehouse")
public class WarehouseConfig {
    private String url;
}