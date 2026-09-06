package vn.warehouse.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Поддержка Java 8 date/time (LocalDateTime и т.д.)
        mapper.registerModule(new JavaTimeModule());

        // Сериализовать даты как строки "2024-01-15T10:30:00", а не [2024,1,15...]
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Не отправлять null-поля в JSON (чистый контракт)
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}