package com.manal.expensemanager.service;

import com.manal.expensemanager.dto.AnnualStatDTO;
import com.manal.expensemanager.dto.CategoryStatDTO;
import com.manal.expensemanager.dto.ExpenseRequestDTO;
import com.manal.expensemanager.dto.MonthlyStatDTO;
import com.manal.expensemanager.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExpenseService {
    Expense createExpense(ExpenseRequestDTO dto);
    List<Expense> getMyExpenses();

    Double getTotalAmount();

    List<MonthlyStatDTO> getMonthlyStats(); // Keep for CSV
    Page<MonthlyStatDTO> getMonthlyStats(Pageable pageable); // New for UI

    List<CategoryStatDTO> getTotalByCategory();
    Page<CategoryStatDTO> getTotalByCategory(Pageable pageable);

    List<AnnualStatDTO> getAnnualStats();
    Page<AnnualStatDTO> getAnnualStats(Pageable pageable);

    List<Expense> getExpensesByUserId(Long userId);

    Double getTotalAmount(Long userId);

    List<MonthlyStatDTO> getMonthlyStats(Long userId);
    Page<MonthlyStatDTO> getMonthlyStats(Long userId, Pageable pageable);

    List<CategoryStatDTO> getTotalByCategory(Long userId);
    Page<CategoryStatDTO> getTotalByCategory(Long userId, Pageable pageable);

    List<AnnualStatDTO> getAnnualStats(Long userId);
    Page<AnnualStatDTO> getAnnualStats(Long userId, Pageable pageable);

}
