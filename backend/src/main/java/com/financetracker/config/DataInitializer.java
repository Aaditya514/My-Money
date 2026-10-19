package com.financetracker.config;

import com.financetracker.entity.Account;
import com.financetracker.entity.AccountType;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.SalaryPlanRepository;
import com.financetracker.repository.SavingsGoalRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final SalaryPlanRepository salaryPlanRepository;
    private final com.financetracker.repository.RecurringPaymentRepository recurringPaymentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           AccountRepository accountRepository,
                           SavingsGoalRepository savingsGoalRepository,
                           SalaryPlanRepository salaryPlanRepository,
                           com.financetracker.repository.RecurringPaymentRepository recurringPaymentRepository,
                           PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.salaryPlanRepository = salaryPlanRepository;
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // 1. Seed Categories if empty
            if (categoryRepository.count() == 0) {
                List<Category> defaultCategories = List.of(
                        // Income categories
                        new Category("Salary", CategoryType.INCOME),
                        new Category("Freelance", CategoryType.INCOME),
                        new Category("Bonus", CategoryType.INCOME),

                        // Expense categories
                        new Category("Food", CategoryType.EXPENSE),
                        new Category("Transport", CategoryType.EXPENSE),
                        new Category("Shopping", CategoryType.EXPENSE),
                        new Category("Bills", CategoryType.EXPENSE),
                        new Category("Entertainment", CategoryType.EXPENSE),
                        new Category("Education", CategoryType.EXPENSE),
                        new Category("Healthcare", CategoryType.EXPENSE),
                        new Category("Other", CategoryType.EXPENSE)
                );

                categoryRepository.saveAll(defaultCategories);
                System.out.println(">>> Seeded " + defaultCategories.size() + " default categories into database.");
            }
        } catch (Exception e) {
            System.err.println(">>> Notice: Could not seed initial categories: " + e.getMessage());
        }
    }
}
