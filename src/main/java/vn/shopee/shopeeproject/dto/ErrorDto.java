package vn.shopee.shopeeproject.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorDto {
    private String code;
    private String header;
    private String message;
}