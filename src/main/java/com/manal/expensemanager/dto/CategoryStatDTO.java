package com.manal.expensemanager.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class CategoryStatDTO {
    private String categoryName;
    private Double totalAmount;
}
