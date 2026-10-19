package com.financetracker.service;

import com.financetracker.dto.AccountRequest;
import com.financetracker.dto.AccountResponse;
import com.financetracker.dto.ReconciliationRequest;
import com.financetracker.entity.Account;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.entity.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtAsc(userId);
        List<AccountResponse> responses = new ArrayList<>();
        for (Account a : accounts) {
            responses.add(mapToResponse(a));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id, Long userId) {
        Account account = getAccountEntity(id, userId);
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountEntity(Long id, Long userId) {
        if (userId != null) {
            return accountRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    public AccountResponse createAccount(AccountRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        if (accountRepository.existsByUserIdAndNameIgnoreCase(user.getId(), request.getName().trim())) {
            throw new RuntimeException("An account with this name already exists: " + request.getName());
        }

        BigDecimal opening = request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO;
        LocalDate openDate = request.getOpeningDate() != null ? request.getOpeningDate() : LocalDate.now();

        Account account = new Account(
                request.getName(),
                request.getAccountType(),
                request.getAccountNumber(),
                opening,
                openDate,
                user
        );

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public AccountResponse updateAccount(Long id, AccountRequest request, Long userId) {
        Account account = getAccountEntity(id, userId);

        account.setName(request.getName());
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }
        account.setAccountNumber(request.getAccountNumber());

        if (request.getOpeningBalance() != null && !request.getOpeningBalance().equals(account.getOpeningBalance())) {
            BigDecimal delta = request.getOpeningBalance().subtract(account.getOpeningBalance());
            account.setOpeningBalance(request.getOpeningBalance());
            account.setCurrentBalance(account.getCurrentBalance().add(delta));
        }

        if (request.getOpeningDate() != null) {
            account.setOpeningDate(request.getOpeningDate());
        }

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public void deleteAccount(Long id, Long userId) {
        Account account = getAccountEntity(id, userId);

        List<Transaction> txs = transactionRepository.findByAccountIdOrderByTransactionDateDesc(id);
        if (!txs.isEmpty()) {
            throw new RuntimeException("Cannot delete account with existing transactions. Please reassign or delete them first.");
        }

        accountRepository.delete(account);
    }

    public AccountResponse reconcileAccount(Long id, ReconciliationRequest request, Long userId) {
        Account account = getAccountEntity(id, userId);

        LocalDate recDate = request.getReconciliationDate() != null ? request.getReconciliationDate() : LocalDate.now();
        account.setLastReconciledDate(recDate);
        account.setLastReconciledBalance(request.getActualBalance());
        account.setCurrentBalance(request.getActualBalance());

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public void adjustBalance(Long accountId, BigDecimal amountDelta) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        account.setCurrentBalance(account.getCurrentBalance().add(amountDelta));
        accountRepository.save(account);
    }

    public AccountResponse recalculateAccountBalance(Long id, Long userId) {
        Account account = getAccountEntity(id, userId);

        BigDecimal balance = account.getOpeningBalance();

        List<Transaction> txs = transactionRepository.findByAccountIdOrderByTransactionDateDesc(id);
        for (Transaction t : txs) {
            if (t.getType() == TransactionType.INCOME || t.getType() == TransactionType.REFUND) {
                balance = balance.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(t.getAmount());
            }
        }

        account.setCurrentBalance(balance);
        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public AccountResponse mapToResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getName(),
                a.getAccountType(),
                a.getAccountNumber(),
                a.getOpeningBalance(),
                a.getCurrentBalance(),
                a.getOpeningDate(),
                a.getLastReconciledDate(),
                a.getLastReconciledBalance(),
                a.getUser().getId(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
