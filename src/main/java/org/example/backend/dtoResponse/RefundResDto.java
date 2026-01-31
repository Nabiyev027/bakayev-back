package org.example.backend.dtoResponse;

import lombok.Data;


import java.time.LocalDate;
import java.util.UUID;

@Data
public class RefundResDto {
    private UUID id;
    private String studentName;
    private Integer amount;
    private String receptionName;
    private LocalDate date;
}
