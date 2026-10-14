package com.bikangu.transactions.controller;

import com.bikangu.transactions.entity.IncomeTransaction;
import com.bikangu.transactions.entity.SavingsTransaction;
import com.bikangu.transactions.entity.Transaction;
import com.bikangu.transactions.repository.IncomeTransactionRepository;
import com.bikangu.transactions.repository.SavingsTransactionRepository;
import com.bikangu.transactions.repository.TransactionRepository;
import com.bikangu.common.response.ApiResponse;
import com.bikangu.authentication.entity.User;
import com.bikangu.common.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionRepository transactionRepository;
    private final IncomeTransactionRepository incomeTransactionRepository;
    private final SavingsTransactionRepository savingsTransactionRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<Transaction> transactions;
        if (startDate != null && endDate != null) {
            Instant start = Instant.parse(startDate);
            Instant end = Instant.parse(endDate);
            transactions = transactionRepository.findByUserIdAndDateRange(user.getId(), start, end,
                    PaginationUtil.createPageable(page, size));
        } else if (type != null) {
            transactions = transactionRepository.findByUserIdAndTransactionType(
                    user.getId(), com.bikangu.common.enums.TransactionType.valueOf(type.toUpperCase()),
                    PaginationUtil.createPageable(page, size));
        } else if (status != null) {
            transactions = transactionRepository.findByUserIdAndStatus(
                    user.getId(), com.bikangu.common.enums.TransactionStatus.valueOf(status.toUpperCase()),
                    PaginationUtil.createPageable(page, size));
        } else {
            transactions = transactionRepository.findByUserId(user.getId(),
                    PaginationUtil.createPageable(page, size));
        }

        return ResponseEntity.ok(ApiResponse.success(transactions, PaginationUtil.toPagination(transactions)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Transaction>> getTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable UUID transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new com.bikangu.common.exception.ResourceNotFoundException("Transaction", transactionId));

        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping("/income")
    public ResponseEntity<ApiResponse<Page<IncomeTransaction>>> getIncomeTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<IncomeTransaction> transactions = incomeTransactionRepository.findByUserId(
                user.getId(), PaginationUtil.createPageable(page, size));

        return ResponseEntity.ok(ApiResponse.success(transactions, PaginationUtil.toPagination(transactions)));
    }

    @GetMapping("/savings")
    public ResponseEntity<ApiResponse<Page<SavingsTransaction>>> getSavingsTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<SavingsTransaction> transactions = savingsTransactionRepository.findByUserId(
                user.getId(), PaginationUtil.createPageable(page, size));

        return ResponseEntity.ok(ApiResponse.success(transactions, PaginationUtil.toPagination(transactions)));
    }
}