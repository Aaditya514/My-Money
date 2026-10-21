package com.financetracker;

import com.financetracker.dto.*;
import com.financetracker.entity.AccountType;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.TransactionType;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.service.AccountService;
import com.financetracker.service.TransactionService;
import com.financetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AccountServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long user1Id;
    private Long foodCategoryId;

    @BeforeEach
    void setUp() {
        AuthResponse user1 = userService.register(new RegisterRequest("Bob", "bob@example.com", "password123", "9876543201"));
        user1Id = user1.getUser().getId();

        Category foodCat = categoryRepository.findByName("Food")
                .orElseGet(() -> categoryRepository.save(new Category("Food", CategoryType.EXPENSE)));
        foodCategoryId = foodCat.getId();
    }

    @Test
    void testCreateAccountAndReconcile() {
        AccountRequest request = new AccountRequest(
                "HDFC Salary",
                AccountType.SALARY,
                "XXXX1234",
                new BigDecimal("25000.00"),
                LocalDate.now(),
                user1Id
        );

        AccountResponse created = accountService.createAccount(request);
        assertNotNull(created.getId());
        assertEquals("HDFC Salary", created.getName());
        assertEquals(new BigDecimal("25000.00"), created.getOpeningBalance());
        assertEquals(new BigDecimal("25000.00"), created.getCurrentBalance());

        // Test reconciliation to actual balance
        ReconciliationRequest recReq = new ReconciliationRequest(
                new BigDecimal("24500.00"),
                "Reconciled with HDFC NetBanking",
                LocalDate.now(),
                user1Id
        );
        AccountResponse reconciled = accountService.reconcileAccount(created.getId(), recReq, user1Id);
        assertEquals(new BigDecimal("24500.00"), reconciled.getCurrentBalance());
        assertEquals(new BigDecimal("24500.00"), reconciled.getLastReconciledBalance());
        assertNotNull(reconciled.getLastReconciledDate());
    }

    @Test
    void testPreventDuplicateAccountNameForSameUser() {
        AccountRequest request1 = new AccountRequest("Wallet", AccountType.WALLET, null, new BigDecimal("1000.00"), LocalDate.now(), user1Id);
        accountService.createAccount(request1);

        AccountRequest request2 = new AccountRequest("Wallet", AccountType.WALLET, null, new BigDecimal("500.00"), LocalDate.now(), user1Id);
        assertThrows(RuntimeException.class, () -> accountService.createAccount(request2));
    }

    @Test
    void testTransactionUpdatesAccountBalance() {
        AccountResponse salaryAcc = accountService.createAccount(
                new AccountRequest("Salary", AccountType.SALARY, null, new BigDecimal("20000.00"), LocalDate.now(), user1Id));

        TransactionRequest expenseReq = new TransactionRequest();
        expenseReq.setTitle("Dinner");
        expenseReq.setAmount(new BigDecimal("500.00"));
        expenseReq.setType(TransactionType.EXPENSE);
        expenseReq.setTransactionDate(LocalDate.now());
        expenseReq.setCategoryId(foodCategoryId);
        expenseReq.setAccountId(salaryAcc.getId());
        expenseReq.setUserId(user1Id);

        TransactionResponse tx = transactionService.createTransaction(expenseReq);
        assertEquals(salaryAcc.getId(), tx.getAccountId());

        AccountResponse afterExpense = accountService.getAccountById(salaryAcc.getId(), user1Id);
        // 20000 - 500 = 19500
        assertEquals(new BigDecimal("19500.00"), afterExpense.getCurrentBalance());

        // Deleting transaction restores balance
        transactionService.deleteTransaction(tx.getId());
        AccountResponse afterDelete = accountService.getAccountById(salaryAcc.getId(), user1Id);
        assertEquals(new BigDecimal("20000.00"), afterDelete.getCurrentBalance());
    }
}
