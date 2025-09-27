package com.manal.expensemanager.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequestDTO {

    @NotNull(message = "category ID is required")
    private Long categoryId;

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Double amount;

    @NotNull(message = "date is required")
    private LocalDate date;
}
