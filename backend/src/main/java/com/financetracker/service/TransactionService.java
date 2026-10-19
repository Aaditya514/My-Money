package com.financetracker.service;

import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.entity.Account;
import com.financetracker.entity.Category;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.entity.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              UserService userService,
                              CategoryService categoryService,
                              AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUserId(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        List<TransactionResponse> responseList = new ArrayList<>();

        for (Transaction t : transactions) {
            responseList.add(mapToResponse(t));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId);
        List<TransactionResponse> responseList = new ArrayList<>();
        for (Transaction t : transactions) {
            responseList.add(mapToResponse(t));
        }
        return responseList;
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = userService.getUserById(request.getUserId());
        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        Transaction transaction = new Transaction();
        transaction.setTitle(request.getTitle());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);
        transaction.setUser(user);

        // Account association and balance update
        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            transaction.setAccount(account);
            applyBalanceChange(account, request.getType(), request.getAmount());
        }

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        Category category = categoryService.getCategoryEntityById(request.getCategoryId());

        // Revert previous account balance impact
        if (transaction.getAccount() != null) {
            revertBalanceChange(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        }

        transaction.setTitle(request.getTitle());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);

        // Apply new account balance impact
        if (request.getAccountId() != null) {
            Account newAccount = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            transaction.setAccount(newAccount);
            applyBalanceChange(newAccount, request.getType(), request.getAmount());
        } else {
            transaction.setAccount(null);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        // Revert balance change
        if (transaction.getAccount() != null) {
            revertBalanceChange(transaction.getAccount(), transaction.getType(), transaction.getAmount());
        }

        transactionRepository.delete(transaction);
    }

    private void applyBalanceChange(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME || type == TransactionType.REFUND) {
            account.setCurrentBalance(account.getCurrentBalance().add(amount));
        } else if (type == TransactionType.EXPENSE) {
            account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        }
        accountRepository.save(account);
    }

    private void revertBalanceChange(Account account, TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME || type == TransactionType.REFUND) {
            account.setCurrentBalance(account.getCurrentBalance().subtract(amount));
        } else if (type == TransactionType.EXPENSE) {
            account.setCurrentBalance(account.getCurrentBalance().add(amount));
        }
        accountRepository.save(account);
    }

    public TransactionResponse mapToResponse(Transaction t) {
        Long accountId = t.getAccount() != null ? t.getAccount().getId() : null;
        String accountName = t.getAccount() != null ? t.getAccount().getName() : null;

        return new TransactionResponse(
                t.getId(),
                t.getTitle(),
                t.getAmount(),
                t.getType(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                accountId,
                accountName,
                t.getUser().getId(),
                t.getCreatedAt()
        );
    }
}
