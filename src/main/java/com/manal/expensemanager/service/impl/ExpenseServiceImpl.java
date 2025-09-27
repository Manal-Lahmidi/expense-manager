package com.manal.expensemanager.service.impl;

import com.manal.expensemanager.dto.AnnualStatDTO;
import com.manal.expensemanager.dto.CategoryStatDTO;
import com.manal.expensemanager.dto.ExpenseRequestDTO;
import com.manal.expensemanager.dto.MonthlyStatDTO;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.model.Expense;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.CategoryRepository;
import com.manal.expensemanager.repository.ExpenseRepository;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.security.CurrentUser;
import com.manal.expensemanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    /* ----------------- Helpers ----------------- */

    private static <T> Page<T> toPage(List<T> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<T> page = all.subList(start, end);
        return new PageImpl<>(page, pageable, all.size());
    }

    /* ----------------- Commands ----------------- */

    @Override
    @Transactional
    public Expense createExpense(ExpenseRequestDTO dto) {
        User user = currentUser.get();
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Expense expense = Expense.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .user(user)
                .category(category)
                .build();

        return expenseRepository.save(expense);
    }

    /* ----------------- Queries: current user ----------------- */

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getMyExpenses() {
        return expenseRepository.findByUser(currentUser.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalAmount() {
        return getTotalAmount(currentUser.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatDTO> getTotalByCategory() {
        return getTotalByCategory(currentUser.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyStatDTO> getMonthlyStats() {
        return getMonthlyStats(currentUser.id());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnualStatDTO> getAnnualStats() {
        return getAnnualStats(currentUser.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryStatDTO> getTotalByCategory(Pageable pageable) {
        return getTotalByCategory(currentUser.id(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonthlyStatDTO> getMonthlyStats(Pageable pageable) {
        return getMonthlyStats(currentUser.id(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnualStatDTO> getAnnualStats(Pageable pageable) {
        return getAnnualStats(currentUser.id(), pageable);
    }

    /* ----------------- Queries: admin by userId ----------------- */

    @Override
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByUserId(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return expenseRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalAmount(Long userId) {
        return expenseRepository.getTotalAmountByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatDTO> getTotalByCategory(Long userId) {
        return expenseRepository.getTotalByCategory(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryStatDTO> getTotalByCategory(Long userId, Pageable pageable) {
        return toPage(getTotalByCategory(userId), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyStatDTO> getMonthlyStats(Long userId) {
        return expenseRepository.getMonthlyStats(userId).stream()
                .map(r -> new MonthlyStatDTO((String) r[0], ((Number) r[1]).doubleValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonthlyStatDTO> getMonthlyStats(Long userId, Pageable pageable) {
        return toPage(getMonthlyStats(userId), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnualStatDTO> getAnnualStats(Long userId) {
        return expenseRepository.getAnnualStats(userId).stream()
                .map(r -> new AnnualStatDTO(String.valueOf(r[0]), ((Number) r[1]).doubleValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnualStatDTO> getAnnualStats(Long userId, Pageable pageable) {
        return toPage(getAnnualStats(userId), pageable);
    }
}
