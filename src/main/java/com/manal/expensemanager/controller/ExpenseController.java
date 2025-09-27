package com.manal.expensemanager.controller;

import com.manal.expensemanager.dto.AnnualStatDTO;
import com.manal.expensemanager.dto.CategoryStatDTO;
import com.manal.expensemanager.dto.ExpenseRequestDTO;
import com.manal.expensemanager.dto.MonthlyStatDTO;
import com.manal.expensemanager.model.Expense;
import com.manal.expensemanager.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public Expense createExpense(@RequestBody @Valid ExpenseRequestDTO dto) {
        return expenseService.createExpense(dto);
    }

    @GetMapping
    public List<Expense> getMine() {
        return expenseService.getMyExpenses();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalAmount() {
        return ResponseEntity.ok(expenseService.getTotalAmount());
    }

    @GetMapping("/monthly")
    public ResponseEntity<Page<MonthlyStatDTO>> getMonthlyStats(
            @PageableDefault(size = 12, sort = "month", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getMonthlyStats(pageable));
    }

    @GetMapping("/by-category")
    public ResponseEntity<Page<CategoryStatDTO>> getTotalByCategory(
            @PageableDefault(size = 10, sort = "category", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getTotalByCategory(pageable));
    }

    @GetMapping("/annual")
    public ResponseEntity<Page<AnnualStatDTO>> getAnnualStats(
            @PageableDefault(size = 10, sort = "year", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAnnualStats(pageable));
    }

    @GetMapping("/annual/export")
    public void exportAnnualStats(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=annual-stats.csv");

        var stats = expenseService.getAnnualStats();
        var writer = response.getWriter();
        writer.println("Year,Total");

        for (var s : stats) {
            writer.printf("%s,%.2f%n", s.getYear(), s.getTotalAmount());
        }

        writer.flush();
    }

    @GetMapping("/admin/by-user")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Expense> adminGetByUser(@RequestParam Long userId) {
        return expenseService.getExpensesByUserId(userId);
    }

    // ExpenseController.java

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/total")
    public ResponseEntity<Double> adminGetTotal(@RequestParam Long userId) {
        return ResponseEntity.ok(expenseService.getTotalAmount(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/monthly")
    public ResponseEntity<Page<MonthlyStatDTO>> adminGetMonthly(
            @RequestParam Long userId,
            @PageableDefault(size = 12, sort = "month", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getMonthlyStats(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/by-category")
    public ResponseEntity<Page<CategoryStatDTO>> adminGetByCategory(
            @RequestParam Long userId,
            @PageableDefault(size = 10, sort = "category", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getTotalByCategory(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/annual")
    public ResponseEntity<Page<AnnualStatDTO>> adminGetAnnual(
            @RequestParam Long userId,
            @PageableDefault(size = 10, sort = "year", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAnnualStats(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/annual/export")
    public void adminExportAnnual(@RequestParam Long userId, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=annual-stats-" + userId + ".csv");

        var stats = expenseService.getAnnualStats(userId);
        var writer = response.getWriter();
        writer.println("Year,Total");
        for (var s : stats) {
            writer.printf("%s,%.2f%n", s.getYear(), s.getTotalAmount());
        }
        writer.flush();

    }



}
