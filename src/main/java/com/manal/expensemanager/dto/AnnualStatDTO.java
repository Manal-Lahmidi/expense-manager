package com.manal.expensemanager.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class AnnualStatDTO {
    private String year;
    private Double totalAmount;
}