package com.financetracker;

import com.financetracker.dto.AuthResponse;
import com.financetracker.dto.RegisterRequest;
import com.financetracker.dto.SalaryPlanRequest;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.TransactionType;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.service.ExportService;
import com.financetracker.service.SalaryPlanService;
import com.financetracker.service.TransactionService;
import com.financetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ExportServiceTest {

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private SalaryPlanService salaryPlanService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        AuthResponse auth = userService.register(new RegisterRequest("Exporter", "export@example.com", "password123", "9876543202"));
        userId = auth.getUser().getId();
    }

    @Test
    void testExportTransactionsCsv() {
        Category foodCat = categoryRepository.findByName("Food")
                .orElseGet(() -> categoryRepository.save(new Category("Food", CategoryType.EXPENSE)));

        TransactionRequest req = new TransactionRequest();
        req.setTitle("Office Lunch");
        req.setAmount(new BigDecimal("250.00"));
        req.setType(TransactionType.EXPENSE);
        req.setTransactionDate(LocalDate.now());
        req.setCategoryId(foodCat.getId());
        req.setUserId(userId);
        transactionService.createTransaction(req);

        byte[] csvBytes = exportService.exportTransactionsCsv(userId, null, null);
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("ID,Date,Title,Type,Category,Account,Amount,Description"));
        assertTrue(csvString.contains("Office Lunch"));
        assertTrue(csvString.contains("250.00"));
    }

    @Test
    void testExportSalaryPlanCsv() {
        SalaryPlanRequest planReq = new SalaryPlanRequest();
        planReq.setUserId(userId);
        planReq.setMonth(10);
        planReq.setYear(2026);
        planReq.setTakeHomeSalary(new BigDecimal("60000.00"));
        planReq.setPlannedExpenses(new BigDecimal("30000.00"));
        planReq.setPlannedEmergencyFund(new BigDecimal("15000.00"));
        salaryPlanService.saveSalaryPlan(planReq);

        byte[] csvBytes = exportService.exportSalaryPlanCsv(userId, 10, 2026);
        assertNotNull(csvBytes);
        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("MONTHLY SALARY PLAN & BUDGET REPORT"));
        assertTrue(csvString.contains("Total Income / Take-Home,60000.00"));
        assertTrue(csvString.contains("Total Expenses,30000.00"));
        assertTrue(csvString.contains("Calculated Buffer (Net Leftover),15000.00"));
    }
}
