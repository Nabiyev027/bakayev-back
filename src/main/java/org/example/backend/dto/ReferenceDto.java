package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReferenceDto {
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    private String telegramUserName;
}
