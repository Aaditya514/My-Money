package com.financetracker;

import com.financetracker.dto.*;
import com.financetracker.entity.*;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SavingsGoalAndSalaryPlanTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SavingsGoalService savingsGoalService;

    @Autowired
    private SalaryPlanService salaryPlanService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long userId;
    private Long salaryAccId;
    private Long savingsAccId;
    private Long foodCatId;
    private Long salaryCatId;

    @BeforeEach
    void setUp() {
        AuthResponse userResp = userService.register(new RegisterRequest("Alice", "alice@example.com", "password123", "9876543204"));
        userId = userResp.getUser().getId();

        AccountResponse salaryAcc = accountService.createAccount(
                new AccountRequest("Salary Acc", AccountType.SALARY, null, new BigDecimal("50000.00"), LocalDate.now(), userId));
        salaryAccId = salaryAcc.getId();

        AccountResponse savingsAcc = accountService.createAccount(
                new AccountRequest("Emergency Savings Acc", AccountType.SAVINGS, null, new BigDecimal("85000.00"), LocalDate.now(), userId));
        savingsAccId = savingsAcc.getId();

        Category foodCat = categoryRepository.findByName("Food")
                .orElseGet(() -> categoryRepository.save(new Category("Food", CategoryType.EXPENSE)));
        foodCatId = foodCat.getId();

        Category salCat = categoryRepository.findByName("Salary")
                .orElseGet(() -> categoryRepository.save(new Category("Salary", CategoryType.INCOME)));
        salaryCatId = salCat.getId();
    }

    @Test
    void testCreateGoalAndContribute() {
        SavingsGoalRequest goalReq = new SavingsGoalRequest(
                "Emergency Fund",
                new BigDecimal("100000.00"),
                new BigDecimal("85000.00"),
                LocalDate.now().plusMonths(6),
                "6-month emergency reserve",
                savingsAccId,
                userId
        );

        SavingsGoalResponse goal = savingsGoalService.createGoal(goalReq);
        assertNotNull(goal.getId());
        assertEquals("Emergency Fund", goal.getName());
        assertEquals(new BigDecimal("100000.00"), goal.getTargetAmount());
        assertEquals(new BigDecimal("85000.00"), goal.getCurrentAmount());
        assertEquals(85.0, goal.getProgressPercentage(), 0.01);
        assertFalse(goal.isCompleted());

        GoalContributionRequest contReq = new GoalContributionRequest(
                new BigDecimal("10000.00"),
                LocalDate.now(),
                "Monthly emergency fund contribution",
                salaryAccId,
                userId
        );

        GoalContributionResponse contResp = savingsGoalService.contributeToGoal(goal.getId(), contReq, userId);
        assertNotNull(contResp.getId());
        assertEquals(new BigDecimal("10000.00"), contResp.getAmount());

        SavingsGoalResponse updatedGoal = savingsGoalService.getGoalById(goal.getId(), userId);
        assertEquals(new BigDecimal("95000.00"), updatedGoal.getCurrentAmount());
        assertEquals(95.0, updatedGoal.getProgressPercentage(), 0.01);

        AccountResponse updatedSalaryAcc = accountService.getAccountById(salaryAccId, userId);
        assertEquals(new BigDecimal("40000.00"), updatedSalaryAcc.getCurrentBalance());

        AccountResponse updatedSavingsAcc = accountService.getAccountById(savingsAccId, userId);
        assertEquals(new BigDecimal("95000.00"), updatedSavingsAcc.getCurrentBalance());

        List<GoalContributionResponse> history = savingsGoalService.getContributions(goal.getId(), userId);
        assertEquals(1, history.size());
        assertEquals(new BigDecimal("10000.00"), history.get(0).getAmount());
    }

    @Test
    void testToggleGoalComplete() {
        SavingsGoalRequest goalReq = new SavingsGoalRequest(
                "Laptop Fund",
                new BigDecimal("60000.00"),
                new BigDecimal("30000.00"),
                LocalDate.now().plusMonths(2),
                "Work laptop",
                salaryAccId,
                userId
        );

        SavingsGoalResponse goal = savingsGoalService.createGoal(goalReq);
        assertFalse(goal.isCompleted());

        SavingsGoalResponse toggled = savingsGoalService.toggleComplete(goal.getId(), userId);
        assertTrue(toggled.isCompleted());
        assertNotNull(toggled.getCompletedAt());

        SavingsGoalResponse unToggled = savingsGoalService.toggleComplete(goal.getId(), userId);
        assertFalse(unToggled.isCompleted());
        assertNull(unToggled.getCompletedAt());
    }

    @Test
    void testSalaryPlanUnconfiguredDefaults() {
        LocalDate today = LocalDate.now();
        SalaryPlanResponse plan = salaryPlanService.getSalaryPlan(userId, today.getMonthValue(), today.getYear());

        assertNotNull(plan);
        assertFalse(plan.isConfigured());
        assertEquals(BigDecimal.ZERO, plan.getTotalPlannedIncome());
        assertEquals(BigDecimal.ZERO, plan.getTotalPlannedExpenses());
        assertEquals(BigDecimal.ZERO, plan.getTotalPlannedSavings());
        assertEquals(BigDecimal.ZERO, plan.getCalculatedBuffer());
        assertFalse(plan.isOverAllocated());
    }

    @Test
    void testSalaryPlanCustomConfigAndMultiIncome() {
        LocalDate today = LocalDate.now();

        SalaryPlanRequest req = new SalaryPlanRequest();
        req.setMonth(today.getMonthValue());
        req.setYear(today.getYear());
        req.setUserId(userId);
        req.setNotes("Comprehensive dynamic budget");

        // Multiple income sources
        req.getIncomeSources().add(new SalaryPlanIncomeSourceDto(null, "Primary Salary", new BigDecimal("50000.00"), 0));
        req.getIncomeSources().add(new SalaryPlanIncomeSourceDto(null, "Freelance Consulting", new BigDecimal("15000.00"), 1));

        // Custom allocations across categories
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Rent", new BigDecimal("20000.00"), SalaryPlanAllocationType.EXPENSE, null, 0));
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Groceries", new BigDecimal("10000.00"), SalaryPlanAllocationType.EXPENSE, foodCatId, 1));
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Emergency Reserve", new BigDecimal("10000.00"), SalaryPlanAllocationType.SAVINGS, null, 2));
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Vacation Savings", new BigDecimal("10000.00"), SalaryPlanAllocationType.SAVINGS, null, 3));
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Buffer / Other", new BigDecimal("5000.00"), SalaryPlanAllocationType.OTHER, null, 4));

        SalaryPlanResponse saved = salaryPlanService.saveSalaryPlan(req);

        assertTrue(saved.isConfigured());
        assertEquals(new BigDecimal("65000.00"), saved.getTotalPlannedIncome());
        assertEquals(new BigDecimal("30000.00"), saved.getTotalPlannedExpenses());
        assertEquals(new BigDecimal("20000.00"), saved.getTotalPlannedSavings());
        assertEquals(new BigDecimal("5000.00"), saved.getTotalPlannedOther());
        assertEquals(new BigDecimal("55000.00"), saved.getTotalPlannedAllocations());
        // Remaining buffer: 65,000 - 55,000 = 10,000
        assertEquals(new BigDecimal("10000.00"), saved.getCalculatedBuffer());
        assertFalse(saved.isOverAllocated());
        assertEquals(BigDecimal.ZERO, saved.getOverAllocatedAmount());
    }

    @Test
    void testSalaryPlanOverAllocationAllowsNegativeBuffer() {
        LocalDate today = LocalDate.now();

        SalaryPlanRequest req = new SalaryPlanRequest();
        req.setMonth(today.getMonthValue());
        req.setYear(today.getYear());
        req.setUserId(userId);
        req.setTakeHomeSalary(new BigDecimal("30000.00")); // total income 30k

        // Allocations total 45,000 (> income)
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Rent", new BigDecimal("25000.00"), SalaryPlanAllocationType.EXPENSE, null, 0));
        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Loan EMI", new BigDecimal("20000.00"), SalaryPlanAllocationType.EXPENSE, null, 1));

        SalaryPlanResponse saved = salaryPlanService.saveSalaryPlan(req);

        // Does NOT throw or block; preserves exact negative buffer
        assertEquals(new BigDecimal("30000.00"), saved.getTotalPlannedIncome());
        assertEquals(new BigDecimal("45000.00"), saved.getTotalPlannedAllocations());
        assertEquals(new BigDecimal("-15000.00"), saved.getCalculatedBuffer());
        assertTrue(saved.isOverAllocated());
        assertEquals(new BigDecimal("15000.00"), saved.getOverAllocatedAmount());
    }

    @Test
    void testSalaryPlanZeroIncomeAndVariableIncome() {
        SalaryPlanRequest req = new SalaryPlanRequest();
        req.setMonth(8);
        req.setYear(2026);
        req.setUserId(userId);
        req.setTakeHomeSalary(BigDecimal.ZERO); // Zero income allowed

        req.getAllocations().add(new SalaryPlanAllocationDto(null, "Living Expenses", new BigDecimal("12000.00"), SalaryPlanAllocationType.EXPENSE, null, 0));

        SalaryPlanResponse saved = salaryPlanService.saveSalaryPlan(req);
        assertEquals(BigDecimal.ZERO, saved.getTotalPlannedIncome());
        assertEquals(new BigDecimal("-12000.00"), saved.getCalculatedBuffer());
        assertTrue(saved.isOverAllocated());
    }

    @Test
    void testSalaryPlanCopyPreviousMonthWithCarryForward() {
        // Month 1 plan
        SalaryPlanRequest reqMonth1 = new SalaryPlanRequest();
        reqMonth1.setMonth(3);
        reqMonth1.setYear(2026);
        reqMonth1.setUserId(userId);
        reqMonth1.setTakeHomeSalary(new BigDecimal("50000.00"));
        reqMonth1.getAllocations().add(new SalaryPlanAllocationDto(null, "Living Expenses", new BigDecimal("35000.00"), SalaryPlanAllocationType.EXPENSE, null, 0));

        SalaryPlanResponse m1 = salaryPlanService.saveSalaryPlan(reqMonth1);
        assertEquals(new BigDecimal("15000.00"), m1.getCalculatedBuffer());

        // Copy Month 1 to Month 2 with carry-forward
        SalaryPlanCopyRequest copyReq = new SalaryPlanCopyRequest(3, 2026, 4, 2026, true, userId);
        SalaryPlanResponse m2 = salaryPlanService.copySalaryPlan(copyReq);

        assertEquals(new BigDecimal("50000.00"), m2.getTotalPlannedIncome());
        assertEquals(new BigDecimal("15000.00"), m2.getUnallocatedCarryForward());
        assertEquals(new BigDecimal("35000.00"), m2.getTotalPlannedExpenses());
        // Buffer: 50,000 + 15,000 (carry-forward) - 35,000 = 30,000
        assertEquals(new BigDecimal("30000.00"), m2.getCalculatedBuffer());
    }

    @Test
    void testSalaryPlanDeletePreservesActualTransactions() {
        LocalDate today = LocalDate.now();

        // 1. Create an actual transaction
        TransactionRequest tx = new TransactionRequest();
        tx.setTitle("Groceries Mart");
        tx.setAmount(new BigDecimal("4500.00"));
        tx.setType(TransactionType.EXPENSE);
        tx.setTransactionDate(today);
        tx.setCategoryId(foodCatId);
        tx.setAccountId(salaryAccId);
        tx.setUserId(userId);
        TransactionResponse createdTx = transactionService.createTransaction(tx);
        assertNotNull(createdTx.getId());

        // 2. Save a salary plan for today
        SalaryPlanRequest planReq = new SalaryPlanRequest();
        planReq.setMonth(today.getMonthValue());
        planReq.setYear(today.getYear());
        planReq.setUserId(userId);
        planReq.setTakeHomeSalary(new BigDecimal("50000.00"));
        SalaryPlanResponse plan = salaryPlanService.saveSalaryPlan(planReq);
        assertNotNull(plan.getId());

        // 3. Delete salary plan
        salaryPlanService.deleteSalaryPlan(plan.getId(), userId);

        // 4. Verify transaction still exists untouched
        TransactionResponse foundTx = transactionService.getTransactionById(createdTx.getId());
        assertNotNull(foundTx);
        assertEquals(new BigDecimal("4500.00"), foundTx.getAmount());
    }

    @Test
    void testCategoryArchiveAndReorder() {
        CategoryRequest catReq = new CategoryRequest("Gym & Sports", CategoryType.EXPENSE, false, 5, userId);
        CategoryResponse created = categoryService.createCategory(catReq);
        assertNotNull(created.getId());
        assertFalse(created.isArchived());

        // Archive category
        CategoryResponse archived = categoryService.archiveCategory(created.getId(), true);
        assertTrue(archived.isArchived());

        // Excluded from active categories list
        List<CategoryResponse> activeCats = categoryService.getAllCategories(userId, false);
        assertFalse(activeCats.stream().anyMatch(c -> c.getId().equals(created.getId())));

        // Included when includeArchived = true
        List<CategoryResponse> allCats = categoryService.getAllCategories(userId, true);
        assertTrue(allCats.stream().anyMatch(c -> c.getId().equals(created.getId())));

        // Unarchive
        CategoryResponse unarchived = categoryService.archiveCategory(created.getId(), false);
        assertFalse(unarchived.isArchived());
    }
}
