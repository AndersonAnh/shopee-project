package vn.shopee.shopeeproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan("vn.shopee.shopeeproject.config")
@EnableRetry
public class ShopeeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopeeProjectApplication.class, args);
    }

}
