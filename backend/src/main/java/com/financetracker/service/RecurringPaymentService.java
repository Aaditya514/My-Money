package com.financetracker.service;

import com.financetracker.dto.RecurringPaymentRecordResult;
import com.financetracker.dto.RecurringPaymentRequest;
import com.financetracker.dto.RecurringPaymentResponse;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.entity.Category;
import com.financetracker.entity.RecurringPayment;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.RecurringPaymentRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public RecurringPaymentService(RecurringPaymentRepository recurringPaymentRepository,
                                   UserRepository userRepository,
                                   CategoryRepository categoryRepository,
                                   TransactionRepository transactionRepository,
                                   TransactionService transactionService) {
        this.recurringPaymentRepository = recurringPaymentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public List<RecurringPaymentResponse> getRecurringPaymentsByUserId(Long userId) {
        List<RecurringPayment> list = recurringPaymentRepository.findByUserIdOrderByNextDueDateAsc(userId);
        List<RecurringPaymentResponse> responseList = new ArrayList<>();
        for (RecurringPayment rp : list) {
            responseList.add(mapToResponse(rp));
        }
        return responseList;
    }

    @Transactional(readOnly = true)
    public RecurringPaymentResponse getRecurringPaymentById(Long id, Long userId) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found with id: " + id));
        return mapToResponse(rp);
    }

    public RecurringPaymentResponse createRecurringPayment(RecurringPaymentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        LocalDate nextDue = request.getNextDueDate() != null ? request.getNextDueDate() : request.getStartDate();

        RecurringPayment rp = new RecurringPayment(
                request.getTitle(),
                request.getAmount(),
                request.getType(),
                category,
                request.getFrequency(),
                request.getStartDate(),
                nextDue,
                request.getNotes(),
                user
        );

        if (request.getActive() != null) {
            rp.setActive(request.getActive());
        }

        RecurringPayment saved = recurringPaymentRepository.save(rp);
        return mapToResponse(saved);
    }

    public RecurringPaymentResponse updateRecurringPayment(Long id, RecurringPaymentRequest request, Long userId) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        rp.setTitle(request.getTitle());
        rp.setAmount(request.getAmount());
        rp.setType(request.getType());
        rp.setCategory(category);
        rp.setFrequency(request.getFrequency());
        rp.setStartDate(request.getStartDate());
        if (request.getNextDueDate() != null) {
            rp.setNextDueDate(request.getNextDueDate());
        }
        rp.setNotes(request.getNotes());
        if (request.getActive() != null) {
            rp.setActive(request.getActive());
        }

        RecurringPayment updated = recurringPaymentRepository.save(rp);
        return mapToResponse(updated);
    }

    public void deleteRecurringPayment(Long id, Long userId) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found with id: " + id));
        recurringPaymentRepository.delete(rp);
    }

    public RecurringPaymentResponse toggleActive(Long id, Long userId) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found with id: " + id));

        rp.setActive(!rp.isActive());
        RecurringPayment updated = recurringPaymentRepository.save(rp);
        return mapToResponse(updated);
    }

    /**
     * Record an instance of this repeating payment into the actual transaction ledger,
     * and automatically advance the next due date by its frequency interval.
     */
    public RecurringPaymentRecordResult recordPayment(Long id, Long userId) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recurring payment not found with id: " + id));

        LocalDate txDate = rp.getNextDueDate() != null ? rp.getNextDueDate() : LocalDate.now();

        // 1. Create and persist transaction
        Transaction tx = new Transaction();
        tx.setTitle(rp.getTitle());
        tx.setAmount(rp.getAmount());
        tx.setType(rp.getType());
        tx.setCategory(rp.getCategory());
        tx.setTransactionDate(txDate);
        tx.setUser(rp.getUser());

        String description = "Recurring payment: " + rp.getTitle() + " (" + rp.getFrequency() + ")";
        if (rp.getNotes() != null && !rp.getNotes().isBlank()) {
            description += " - " + rp.getNotes();
        }
        tx.setDescription(description);

        Transaction savedTx = transactionRepository.save(tx);
        TransactionResponse txResponse = transactionService.mapToResponse(savedTx);

        // 2. Advance the recurring payment next due date
        LocalDate previousDueDate = rp.getNextDueDate();
        rp.advanceNextDueDate();
        RecurringPayment updatedRp = recurringPaymentRepository.save(rp);

        String message = String.format("Successfully recorded payment of ₹%s for '%s'. Next due date advanced to %s.",
                rp.getAmount().toPlainString(), rp.getTitle(), updatedRp.getNextDueDate());

        return new RecurringPaymentRecordResult(mapToResponse(updatedRp), txResponse, message);
    }

    @Transactional(readOnly = true)
    public List<RecurringPaymentResponse> getUpcomingPayments(Long userId, int daysAhead) {
        LocalDate cutoff = LocalDate.now().plusDays(daysAhead);
        List<RecurringPayment> list = recurringPaymentRepository.findByUserIdAndActiveTrueAndNextDueDateLessThanEqual(userId, cutoff);
        List<RecurringPaymentResponse> responses = new ArrayList<>();
        for (RecurringPayment rp : list) {
            responses.add(mapToResponse(rp));
        }
        return responses;
    }

    public RecurringPaymentResponse mapToResponse(RecurringPayment rp) {
        Long catId = rp.getCategory() != null ? rp.getCategory().getId() : null;
        String catName = rp.getCategory() != null ? rp.getCategory().getName() : null;
        Long userId = rp.getUser() != null ? rp.getUser().getId() : null;

        return new RecurringPaymentResponse(
                rp.getId(),
                rp.getTitle(),
                rp.getAmount(),
                rp.getType(),
                catId,
                catName,
                rp.getFrequency(),
                rp.getStartDate(),
                rp.getNextDueDate(),
                rp.isActive(),
                rp.getNotes(),
                userId,
                rp.getCreatedAt(),
                rp.getUpdatedAt()
        );
    }
}
