package com.manal.expensemanager.service;

import com.manal.expensemanager.dto.CategoryRequestDTO;
import com.manal.expensemanager.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category createCategory(CategoryRequestDTO dto);
    List<Category> getAllCategories();

    Optional<Category> getById(Long id);

}
