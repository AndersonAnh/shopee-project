package vn.shopee.shopeeproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan("vn.shopee.shopeeproject.config")
// order > circuit-breaker-aspect-order: retry отрабатывает внутри breaker'а,
// поэтому fallback вызывается только после исчерпания попыток
@EnableRetry(order = 2)
public class ShopeeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopeeProjectApplication.class, args);
    }

}
