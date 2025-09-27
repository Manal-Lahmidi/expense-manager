package com.manal.expensemanager.service;


import com.manal.expensemanager.dto.CategoryRequestDTO;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.repository.CategoryRepository;
import com.manal.expensemanager.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class CategoryServiceImplTest {

    private final CategoryRepository repo = mock(CategoryRepository.class);
    private final CategoryServiceImpl service = new CategoryServiceImpl(repo);

    @Test
    void createCategory_shouldPersist_whenNameIsUnique() {
        // given
        var dto = new CategoryRequestDTO("Food");
        given(repo.existsByName("Food")).willReturn(false);
        var saved = Category.builder().id(10L).name("Food").build();
        given(repo.save(any(Category.class))).willReturn(saved);

        // when
        Category result = service.createCategory(dto);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Food");

        var captor = ArgumentCaptor.forClass(Category.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Food");
    }

    @Test
    void createCategory_shouldFail_whenNameExists() {
        // given
        var dto = new CategoryRequestDTO("Food");
        given(repo.existsByName("Food")).willReturn(true);

        // when/then
        assertThatThrownBy(() -> service.createCategory(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(repo, never()).save(any());
    }

    @Test
    void getAllCategories_shouldReturnAll() {
        // given
        var list = List.of(
                Category.builder().id(1L).name("Food").build(),
                Category.builder().id(2L).name("Tech").build()
        );
        given(repo.findAll()).willReturn(list);

        // when
        var result = service.getAllCategories();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName).containsExactly("Food", "Tech");
    }

    @Test
    void getById_shouldDelegateToRepo() {
        // given
        var cat = Category.builder().id(7L).name("Travel").build();
        given(repo.findById(7L)).willReturn(Optional.of(cat));

        // when
        var result = service.getById(7L);

        // then
        assertThat(result).contains(cat);
    }
}
