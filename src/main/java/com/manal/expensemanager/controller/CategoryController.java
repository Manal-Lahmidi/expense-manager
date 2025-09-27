package com.manal.expensemanager.controller;

import com.manal.expensemanager.dto.CategoryRequestDTO;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody @Valid CategoryRequestDTO dto) {
        Category createdCategory = categoryService.createCategory(dto);
        return ResponseEntity.status(201).body(createdCategory);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    
}

