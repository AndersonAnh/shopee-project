package vn.shopee.shopeeproject.config.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderCircuitBreakerConfig {

    @Bean("orderCircuitBreaker")
    public CircuitBreaker orderCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("orderService");
    }
}