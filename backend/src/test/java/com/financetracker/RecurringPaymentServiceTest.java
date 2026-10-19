package com.financetracker;

import com.financetracker.dto.RecurringPaymentRecordResult;
import com.financetracker.dto.RecurringPaymentRequest;
import com.financetracker.dto.RecurringPaymentResponse;
import com.financetracker.entity.*;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.RecurringPaymentRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import com.financetracker.service.RecurringPaymentService;
import com.financetracker.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RecurringPaymentServiceTest {

    @Autowired
    private RecurringPaymentService recurringPaymentService;

    @Autowired
    private RecurringPaymentRepository recurringPaymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Category housingCategory;

    @BeforeEach
    public void setup() {
        testUser = userRepository.findAll().stream().findFirst()
                .orElseGet(() -> userRepository.save(new User("Test User", "test@recurring.com", "pass")));

        housingCategory = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == CategoryType.EXPENSE)
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category("Housing", CategoryType.EXPENSE)));
    }

    @Test
    public void testCreateAndRetrieveRecurringPayment() {
        RecurringPaymentRequest req = new RecurringPaymentRequest(
                "Flat Rent",
                new BigDecimal("15000.00"),
                TransactionType.EXPENSE,
                housingCategory.getId(),
                RecurrenceFrequency.MONTHLY,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                "Monthly rent payment",
                testUser.getId(),
                true
        );

        RecurringPaymentResponse created = recurringPaymentService.createRecurringPayment(req);
        assertNotNull(created.getId());
        assertEquals("Flat Rent", created.getTitle());
        assertEquals(new BigDecimal("15000.00"), created.getAmount());
        assertEquals(RecurrenceFrequency.MONTHLY, created.getFrequency());

        List<RecurringPaymentResponse> list = recurringPaymentService.getRecurringPaymentsByUserId(testUser.getId());
        assertTrue(list.stream().anyMatch(rp -> rp.getId().equals(created.getId())));
    }

    @Test
    public void testRecordPaymentAdvancesDueDateAndCreatesTransaction() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        RecurringPayment rp = new RecurringPayment(
                "Apartment Rent Test",
                new BigDecimal("18000.00"),
                TransactionType.EXPENSE,
                housingCategory,
                RecurrenceFrequency.MONTHLY,
                start,
                start,
                "Due 1st of month",
                testUser
        );
        rp = recurringPaymentRepository.save(rp);

        RecurringPaymentRecordResult result = recurringPaymentService.recordPayment(rp.getId(), testUser.getId());

        assertNotNull(result);
        assertNotNull(result.getTransaction());
        assertEquals(new BigDecimal("18000.00"), result.getTransaction().getAmount());
        assertEquals("Apartment Rent Test", result.getTransaction().getTitle());

        // Verify the next due date was advanced by 1 month
        assertEquals(LocalDate.of(2026, 12, 1), result.getRecurringPayment().getNextDueDate());

        // Verify transaction exists in db
        Transaction tx = transactionRepository.findById(result.getTransaction().getId()).orElse(null);
        assertNotNull(tx);
        assertEquals(new BigDecimal("18000.00"), tx.getAmount());
    }

    @Test
    public void testToggleActive() {
        RecurringPayment rp = new RecurringPayment(
                "Broadband",
                new BigDecimal("799.00"),
                TransactionType.EXPENSE,
                housingCategory,
                RecurrenceFrequency.MONTHLY,
                LocalDate.now(),
                LocalDate.now(),
                "Internet bill",
                testUser
        );
        rp = recurringPaymentRepository.save(rp);
        assertTrue(rp.isActive());

        RecurringPaymentResponse toggled = recurringPaymentService.toggleActive(rp.getId(), testUser.getId());
        assertFalse(toggled.isActive());

        RecurringPaymentResponse toggledBack = recurringPaymentService.toggleActive(rp.getId(), testUser.getId());
        assertTrue(toggledBack.isActive());
    }
}
