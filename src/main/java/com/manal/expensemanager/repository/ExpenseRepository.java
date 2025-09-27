package com.manal.expensemanager.repository;

import com.manal.expensemanager.dto.CategoryStatDTO;
import com.manal.expensemanager.dto.MonthlyStatDTO;
import com.manal.expensemanager.dto.AnnualStatDTO;

import com.manal.expensemanager.model.Expense;
import com.manal.expensemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double getTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT new com.manal.expensemanager.dto.CategoryStatDTO(e.category.name, SUM(e.amount)) " +
            "FROM Expense e WHERE e.user.id = :userId GROUP BY e.category.name")
    List<CategoryStatDTO> getTotalByCategory(@Param("userId") Long userId);

    @Query(value = """
    SELECT TO_CHAR(e.date, 'YYYY-MM') AS month, SUM(e.amount) AS totalAmount
    FROM expense e
    WHERE e.user_id = :userId
    GROUP BY TO_CHAR(e.date, 'YYYY-MM')
    ORDER BY month
    """, nativeQuery = true)
        List<Object[]> getMonthlyStats(@Param("userId") Long userId);


    @Query(value = "SELECT EXTRACT(YEAR FROM e.date) AS year, SUM(e.amount) AS totalAmount " +
            "FROM expense e WHERE e.user_id = :userId " +
            "GROUP BY EXTRACT(YEAR FROM e.date) ORDER BY year", nativeQuery = true)
    List<Object[]> getAnnualStats(@Param("userId") Long userId);


}
