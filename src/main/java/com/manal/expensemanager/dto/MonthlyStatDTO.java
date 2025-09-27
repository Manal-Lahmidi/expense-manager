package com.manal.expensemanager.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class MonthlyStatDTO {
    private String month; // e.g., "2025-07"
    private Double totalAmount;
}
