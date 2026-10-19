package com.financetracker.controller;

import com.financetracker.dto.AccountRequest;
import com.financetracker.dto.AccountResponse;
import com.financetracker.dto.ReconciliationRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.service.AccountService;
import com.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(@PathVariable Long userId) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id,
                                                          @RequestParam(required = false) Long userId) {
        AccountResponse account = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse created = accountService.createAccount(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable Long id,
                                                         @RequestParam(required = false) Long userId,
                                                         @Valid @RequestBody AccountRequest request) {
        AccountResponse updated = accountService.updateAccount(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id,
                                              @RequestParam(required = false) Long userId) {
        accountService.deleteAccount(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reconcile")
    public ResponseEntity<AccountResponse> reconcileAccount(@PathVariable Long id,
                                                            @RequestParam(required = false) Long userId,
                                                            @Valid @RequestBody ReconciliationRequest request) {
        Long resolvedUserId = userId != null ? userId : request.getUserId();
        AccountResponse reconciled = accountService.reconcileAccount(id, request, resolvedUserId);
        return ResponseEntity.ok(reconciled);
    }

    @PostMapping("/{id}/recalculate")
    public ResponseEntity<AccountResponse> recalculateAccount(@PathVariable Long id,
                                                              @RequestParam(required = false) Long userId) {
        AccountResponse recalculated = accountService.recalculateAccountBalance(id, userId);
        return ResponseEntity.ok(recalculated);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(@PathVariable Long id) {
        List<TransactionResponse> txs = transactionService.getTransactionsByAccountId(id);
        return ResponseEntity.ok(txs);
    }
}
