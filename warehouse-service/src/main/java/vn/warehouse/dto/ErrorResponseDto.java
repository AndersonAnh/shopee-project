package vn.warehouse.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorResponseDto {
    private String code;
    private String message;
}

