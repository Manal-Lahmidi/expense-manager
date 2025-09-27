package com.manal.expensemanager.service;

import com.manal.expensemanager.dto.AnnualStatDTO;
import com.manal.expensemanager.dto.CategoryStatDTO;
import com.manal.expensemanager.dto.ExpenseRequestDTO;
import com.manal.expensemanager.dto.MonthlyStatDTO;
import com.manal.expensemanager.model.Category;
import com.manal.expensemanager.model.Expense;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.CategoryRepository;
import com.manal.expensemanager.repository.ExpenseRepository;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.security.CurrentUser;
import com.manal.expensemanager.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class ExpenseServiceImplTest {

    private final ExpenseRepository expenseRepository = mock(ExpenseRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);

    private ExpenseServiceImpl service;

    private User me;

    @BeforeEach
    void setUp() {
        service = new ExpenseServiceImpl(expenseRepository, userRepository, categoryRepository, currentUser);
        me = User.builder().id(42L).email("me@test.io").fullName("Me").role(Role.USER).password("x").build();
        given(currentUser.get()).willReturn(me);
        given(currentUser.id()).willReturn(42L);
    }

    @Test
    void createExpense_shouldSaveWithCurrentUserAndCategory() {
        // given
        var dto = new com.manal.expensemanager.dto.ExpenseRequestDTO(
                7L, "Coffee", "morning", 3.5, LocalDate.of(2025, 8, 24)
        );
        var cat = Category.builder().id(7L).name("Food").build();
        given(categoryRepository.findById(7L)).willReturn(Optional.of(cat));

        var saved = Expense.builder()
                .id(100L).title("Coffee").description("morning")
                .amount(3.5).date(LocalDate.of(2025, 8, 24))
                .user(me).category(cat).build();
        given(expenseRepository.save(any(Expense.class))).willReturn(saved);

        // when
        Expense result = service.createExpense(dto);

        // then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUser()).isEqualTo(me);
        assertThat(result.getCategory()).isEqualTo(cat);

        var captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        Expense toSave = captor.getValue();
        assertThat(toSave.getTitle()).isEqualTo("Coffee");
        assertThat(toSave.getAmount()).isEqualTo(3.5);
        assertThat(toSave.getUser().getId()).isEqualTo(42L);
        assertThat(toSave.getCategory().getId()).isEqualTo(7L);
    }

    @Test
    void createExpense_shouldThrow_whenCategoryNotFound() {
        // given
        var dto = new ExpenseRequestDTO(99L, "Item", null, 10.0, LocalDate.now());
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> service.createExpense(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found");
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void getMyExpenses_shouldReturnListFromRepo() {
        // given
        var e1 = Expense.builder().id(1L).title("A").user(me).category(Category.builder().id(1L).name("Food").build()).build();
        var e2 = Expense.builder().id(2L).title("B").user(me).category(Category.builder().id(2L).name("Tech").build()).build();
        given(expenseRepository.findByUser(me)).willReturn(List.of(e1, e2));

        // when
        var list = service.getMyExpenses();

        // then
        assertThat(list).hasSize(2).extracting(Expense::getId).containsExactly(1L, 2L);
        verify(expenseRepository).findByUser(me);
    }

    @Test
    void getTotalAmount_shouldQueryByCurrentUserId() {
        // given
        given(expenseRepository.getTotalAmountByUserId(42L)).willReturn(123.45);

        // when
        Double total = service.getTotalAmount();

        // then
        assertThat(total).isEqualTo(123.45);
        verify(expenseRepository).getTotalAmountByUserId(42L);
    }

    @Test
    void getTotalByCategory_shouldReturnDTOs() {
        // given
        var rows = List.of(
                new CategoryStatDTO("Food", 20.0),
                new CategoryStatDTO("Tech", 50.0)
        );
        given(expenseRepository.getTotalByCategory(42L)).willReturn(rows);

        // when
        var result = service.getTotalByCategory();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CategoryStatDTO::getCategoryName).containsExactly("Food", "Tech");
        verify(expenseRepository).getTotalByCategory(42L);
    }

    @Test
    void getMonthlyStats_shouldMapNativeRows() {
        // given (native query returns Object[]: [String month, Number total])
        var nativeRows = List.of(
                new Object[]{"2025-07", 30.0},
                new Object[]{"2025-08", 70.5}
        );
        given(expenseRepository.getMonthlyStats(42L)).willReturn(nativeRows);

        // when
        List<MonthlyStatDTO> result = service.getMonthlyStats();

        // then
        assertThat(result).containsExactly(
                new MonthlyStatDTO("2025-07", 30.0),
                new MonthlyStatDTO("2025-08", 70.5)
        );
        verify(expenseRepository).getMonthlyStats(42L);
    }

    @Test
    void getAnnualStats_shouldMapNativeRows() {
        // given (native query returns Object[]: [year(any type), Number total])
        var rows = List.of(
                new Object[]{2024, 100.0},
                new Object[]{2025, 200.5}
        );
        given(expenseRepository.getAnnualStats(42L)).willReturn(rows);

        // when
        List<AnnualStatDTO> result = service.getAnnualStats();

        // then
        assertThat(result).extracting(AnnualStatDTO::getYear).containsExactly("2024", "2025");
        assertThat(result).extracting(AnnualStatDTO::getTotalAmount).containsExactly(100.0, 200.5);
        verify(expenseRepository).getAnnualStats(42L);
    }
}
