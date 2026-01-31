package org.example.backend.dtoResponse;

import lombok.Data;

import java.util.UUID;

@Data
public class DiscountResDto {
    private UUID id;
    private Integer amount;
    private String endDate;
    private boolean active;
}
