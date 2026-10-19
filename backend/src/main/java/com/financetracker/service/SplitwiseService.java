package com.financetracker.service;

import com.financetracker.dto.*;
import com.financetracker.entity.*;
import com.financetracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class SplitwiseService {

    private final SplitGroupRepository groupRepository;
    private final SplitMemberRepository memberRepository;
    private final SplitExpenseRepository expenseRepository;
    private final SplitExpenseShareRepository shareRepository;
    private final SplitSettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public SplitwiseService(SplitGroupRepository groupRepository,
                            SplitMemberRepository memberRepository,
                            SplitExpenseRepository expenseRepository,
                            SplitExpenseShareRepository shareRepository,
                            SplitSettlementRepository settlementRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            TransactionRepository transactionRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
        this.shareRepository = shareRepository;
        this.settlementRepository = settlementRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    // ==========================================
    // Groups
    // ==========================================

    @Transactional(readOnly = true)
    public List<SplitGroupDTO> getGroupsByUserId(Long userId) {
        List<SplitGroup> groups = groupRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        List<SplitGroupDTO> dtoList = new ArrayList<>();

        for (SplitGroup g : groups) {
            SplitGroupDTO dto = mapToGroupDTO(g, userId);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Transactional(readOnly = true)
    public SplitGroupDTO getGroupById(Long groupId, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        return mapToGroupDTO(group, userId);
    }

    public SplitGroupDTO createGroup(SplitGroupRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        SplitGroup group = new SplitGroup(
                request.getName(),
                request.getDescription(),
                request.getGroupType(),
                user
        );
        SplitGroup savedGroup = groupRepository.save(group);

        // Always add current user as "You"
        String currentUserName = (user.getName() != null && !user.getName().isBlank()) ? user.getName() : "You";
        SplitMember currentUserMember = new SplitMember(currentUserName + " (You)", user.getEmail(), true, savedGroup, user);
        memberRepository.save(currentUserMember);

        // Add additional members if provided
        if (request.getMemberNames() != null) {
            for (String memberName : request.getMemberNames()) {
                if (memberName != null && !memberName.trim().isEmpty()) {
                    String cleanName = memberName.trim();
                    if (!cleanName.equalsIgnoreCase("you") && !cleanName.equalsIgnoreCase(currentUserName)) {
                        SplitMember friend = new SplitMember(cleanName, null, false, savedGroup, user);
                        memberRepository.save(friend);
                    }
                }
            }
        }

        return mapToGroupDTO(savedGroup, userId);
    }

    public SplitGroupDTO updateGroup(Long groupId, SplitGroupRequest request, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));

        group.setName(request.getName());
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        if (request.getGroupType() != null) {
            group.setGroupType(request.getGroupType());
        }

        SplitGroup saved = groupRepository.save(group);
        return mapToGroupDTO(saved, userId);
    }

    public void deleteGroup(Long groupId, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        groupRepository.delete(group);
    }

    // ==========================================
    // Members
    // ==========================================

    @Transactional(readOnly = true)
    public List<SplitMemberDTO> getMembers(Long groupId, Long userId) {
        verifyGroupOwnership(groupId, userId);
        List<SplitMember> members = memberRepository.findByGroupIdOrderByIdAsc(groupId);
        List<SplitMemberDTO> dtoList = new ArrayList<>();
        for (SplitMember m : members) {
            dtoList.add(mapToMemberDTO(m));
        }
        return dtoList;
    }

    public SplitMemberDTO addMember(Long groupId, SplitMemberRequest request, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        User user = group.getUser();

        SplitMember member = new SplitMember(
                request.getName(),
                request.getEmailOrPhone(),
                request.isCurrentUser(),
                group,
                user
        );
        SplitMember saved = memberRepository.save(member);
        return mapToMemberDTO(saved);
    }

    public void removeMember(Long groupId, Long memberId, Long userId) {
        verifyGroupOwnership(groupId, userId);
        SplitMember member = memberRepository.findByIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found in group"));

        if (member.isCurrentUser()) {
            throw new IllegalArgumentException("Cannot remove yourself from the group");
        }

        memberRepository.delete(member);
    }

    // ==========================================
    // Expenses
    // ==========================================

    @Transactional(readOnly = true)
    public List<SplitExpenseResponse> getExpensesByGroupId(Long groupId, Long userId) {
        verifyGroupOwnership(groupId, userId);
        List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId);
        List<SplitExpenseResponse> responses = new ArrayList<>();

        for (SplitExpense expense : expenses) {
            responses.add(mapToExpenseResponse(expense, groupId));
        }
        return responses;
    }

    public SplitExpenseResponse createExpense(Long groupId, SplitExpenseRequest request, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        User user = group.getUser();

        SplitMember payer = memberRepository.findByIdAndGroupId(request.getPaidByMemberId(), groupId)
                .orElseThrow(() -> new IllegalArgumentException("Payer member not found in group"));

        SplitExpense expense = new SplitExpense();
        expense.setTitle(request.getTitle());
        expense.setTotalAmount(request.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
        expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now());
        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setSplitMethod(request.getSplitMethod() != null ? request.getSplitMethod().toUpperCase() : "EQUAL");
        expense.setNotes(request.getNotes());
        expense.setUser(user);

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(expense::setCategory);
        }

        SplitExpense savedExpense = expenseRepository.save(expense);

        // Compute and persist shares
        List<SplitExpenseShare> shares = computeShares(savedExpense, group, request);
        for (SplitExpenseShare share : shares) {
            shareRepository.save(share);
        }
        savedExpense.setShares(shares);

        return mapToExpenseResponse(savedExpense, groupId);
    }

    public SplitExpenseResponse updateExpense(Long groupId, Long expenseId, SplitExpenseRequest request, Long userId) {
        verifyGroupOwnership(groupId, userId);
        SplitExpense expense = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        SplitMember payer = memberRepository.findByIdAndGroupId(request.getPaidByMemberId(), groupId)
                .orElseThrow(() -> new IllegalArgumentException("Payer member not found in group"));

        expense.setTitle(request.getTitle());
        expense.setTotalAmount(request.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        expense.setPaidBy(payer);
        expense.setSplitMethod(request.getSplitMethod() != null ? request.getSplitMethod().toUpperCase() : "EQUAL");
        expense.setNotes(request.getNotes());

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId()).ifPresent(expense::setCategory);
        } else {
            expense.setCategory(null);
        }

        // Clear existing shares and recompute
        shareRepository.deleteAll(expense.getShares());
        expense.getShares().clear();

        List<SplitExpenseShare> newShares = computeShares(expense, expense.getGroup(), request);
        for (SplitExpenseShare s : newShares) {
            shareRepository.save(s);
        }
        expense.setShares(newShares);

        SplitExpense saved = expenseRepository.save(expense);
        return mapToExpenseResponse(saved, groupId);
    }

    public void deleteExpense(Long groupId, Long expenseId, Long userId) {
        verifyGroupOwnership(groupId, userId);
        SplitExpense expense = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // If synced to ledger, remove ledger transaction as well
        if (expense.isSyncedToLedger() && expense.getLedgerTransactionId() != null) {
            try {
                transactionRepository.deleteById(expense.getLedgerTransactionId());
            } catch (Exception ignored) {
            }
        }

        expenseRepository.delete(expense);
    }

    // ==========================================
    // Settlements
    // ==========================================

    @Transactional(readOnly = true)
    public List<SplitSettlementResponse> getSettlementsByGroupId(Long groupId, Long userId) {
        verifyGroupOwnership(groupId, userId);
        List<SplitSettlement> settlements = settlementRepository.findByGroupIdOrderBySettlementDateDescCreatedAtDesc(groupId);
        List<SplitSettlementResponse> responses = new ArrayList<>();
        for (SplitSettlement s : settlements) {
            responses.add(mapToSettlementResponse(s));
        }
        return responses;
    }

    public SplitSettlementResponse createSettlement(Long groupId, SplitSettlementRequest request, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        User user = group.getUser();

        SplitMember paidBy = memberRepository.findByIdAndGroupId(request.getPaidByMemberId(), groupId)
                .orElseThrow(() -> new IllegalArgumentException("Payer member not found"));
        SplitMember paidTo = memberRepository.findByIdAndGroupId(request.getPaidToMemberId(), groupId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient member not found"));

        if (paidBy.getId().equals(paidTo.getId())) {
            throw new IllegalArgumentException("Payer and recipient cannot be the same member");
        }

        SplitSettlement settlement = new SplitSettlement();
        settlement.setGroup(group);
        settlement.setPaidBy(paidBy);
        settlement.setPaidTo(paidTo);
        settlement.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        settlement.setSettlementDate(request.getSettlementDate() != null ? request.getSettlementDate() : LocalDate.now());
        settlement.setNotes(request.getNotes());
        settlement.setUser(user);

        // Optionally record in ledger if current user is involved
        if (request.isRecordInLedger() && (paidBy.isCurrentUser() || paidTo.isCurrentUser())) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(settlement.getAmount());
            transaction.setTransactionDate(settlement.getSettlementDate());

            Category cat = null;
            if (request.getCategoryId() != null) {
                cat = categoryRepository.findById(request.getCategoryId()).orElse(null);
            }
            if (cat == null) {
                List<Category> cats = categoryRepository.findAvailableCategories(user.getId(), false);
                cat = cats.isEmpty() ? null : cats.get(0);
            }
            transaction.setCategory(cat);

            if (paidBy.isCurrentUser()) {
                // User paid friend -> Expense
                transaction.setType(TransactionType.EXPENSE);
                transaction.setTitle("Settlement to " + paidTo.getName() + " (" + group.getName() + ")");
                transaction.setDescription("Settled split debt in group " + group.getName());
            } else {
                // User received from friend -> Income
                transaction.setType(TransactionType.INCOME);
                transaction.setTitle("Settlement received from " + paidBy.getName() + " (" + group.getName() + ")");
                transaction.setDescription("Received split balance reimbursement in " + group.getName());
            }

            Transaction savedTrans = transactionRepository.save(transaction);
            settlement.setRecordedInLedger(true);
            settlement.setLedgerTransactionId(savedTrans.getId());
        }

        SplitSettlement savedSettlement = settlementRepository.save(settlement);
        return mapToSettlementResponse(savedSettlement);
    }

    // ==========================================
    // 1-Tap Sync to Personal Ledger
    // ==========================================

    public SplitExpenseResponse syncExpenseToLedger(Long groupId, Long expenseId, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));
        SplitExpense expense = expenseRepository.findByIdAndGroupId(expenseId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        if (expense.isSyncedToLedger()) {
            return mapToExpenseResponse(expense, groupId);
        }

        // Find user's member and share
        SplitMember currentUserMember = memberRepository.findByGroupIdAndIsCurrentUserTrue(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Current user member not found in group"));

        SplitExpenseShare userShare = expense.getShares().stream()
                .filter(s -> s.getMember().getId().equals(currentUserMember.getId()))
                .findFirst()
                .orElse(null);

        BigDecimal syncAmount;
        if (userShare != null && userShare.getOwedAmount().compareTo(BigDecimal.ZERO) > 0) {
            syncAmount = userShare.getOwedAmount();
        } else if (expense.getPaidBy().getId().equals(currentUserMember.getId())) {
            // User paid, but has 0 share (e.g. treated friends)
            syncAmount = expense.getTotalAmount();
        } else {
            throw new IllegalArgumentException("You have no share or payment to sync for this expense");
        }

        // Choose category
        Category category = expense.getCategory();
        if (category == null) {
            List<Category> expenseCategories = categoryRepository.findAvailableCategoriesByType(userId, CategoryType.EXPENSE, false);
            if (!expenseCategories.isEmpty()) {
                category = expenseCategories.get(0);
            } else {
                category = categoryRepository.findAll().stream()
                        .filter(c -> c.getType() == CategoryType.EXPENSE)
                        .findFirst()
                        .orElse(null);
            }
        }

        Transaction transaction = new Transaction();
        transaction.setTitle("[Split] " + expense.getTitle());
        transaction.setAmount(syncAmount);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setUser(group.getUser());
        transaction.setTransactionDate(expense.getExpenseDate());
        transaction.setDescription("My share for '" + expense.getTitle() + "' in " + group.getName() +
                (expense.getNotes() != null && !expense.getNotes().isBlank() ? " (" + expense.getNotes() + ")" : ""));

        Transaction savedTx = transactionRepository.save(transaction);

        expense.setSyncedToLedger(true);
        expense.setLedgerTransactionId(savedTx.getId());
        expenseRepository.save(expense);

        return mapToExpenseResponse(expense, groupId);
    }

    // ==========================================
    // Balance Summary & Debt Simplification
    // ==========================================

    @Transactional(readOnly = true)
    public SplitBalanceSummaryDTO getBalanceSummary(Long groupId, Long userId) {
        SplitGroup group = groupRepository.findByIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Split group not found with id: " + groupId));

        List<SplitMember> members = memberRepository.findByGroupIdOrderByIdAsc(groupId);
        List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(groupId);
        List<SplitSettlement> settlements = settlementRepository.findByGroupIdOrderBySettlementDateDescCreatedAtDesc(groupId);

        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> owedMap = new HashMap<>();

        for (SplitMember m : members) {
            paidMap.put(m.getId(), BigDecimal.ZERO);
            owedMap.put(m.getId(), BigDecimal.ZERO);
        }

        BigDecimal totalGroupSpending = BigDecimal.ZERO;

        for (SplitExpense exp : expenses) {
            totalGroupSpending = totalGroupSpending.add(exp.getTotalAmount());
            Long payerId = exp.getPaidBy().getId();
            paidMap.put(payerId, paidMap.getOrDefault(payerId, BigDecimal.ZERO).add(exp.getTotalAmount()));

            for (SplitExpenseShare share : exp.getShares()) {
                Long mId = share.getMember().getId();
                owedMap.put(mId, owedMap.getOrDefault(mId, BigDecimal.ZERO).add(share.getOwedAmount()));
            }
        }

        for (SplitSettlement set : settlements) {
            Long payerId = set.getPaidBy().getId();
            Long receiverId = set.getPaidTo().getId();

            paidMap.put(payerId, paidMap.getOrDefault(payerId, BigDecimal.ZERO).add(set.getAmount()));
            owedMap.put(receiverId, owedMap.getOrDefault(receiverId, BigDecimal.ZERO).add(set.getAmount()));
        }

        List<SplitMemberDTO> memberBalances = new ArrayList<>();
        BigDecimal currentUserNet = BigDecimal.ZERO;
        BigDecimal currentUserPaid = BigDecimal.ZERO;
        BigDecimal currentUserShare = BigDecimal.ZERO;

        Map<Long, BigDecimal> netMap = new HashMap<>();

        for (SplitMember m : members) {
            BigDecimal paid = paidMap.getOrDefault(m.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal owed = owedMap.getOrDefault(m.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = paid.subtract(owed).setScale(2, RoundingMode.HALF_UP);

            netMap.put(m.getId(), net);

            SplitMemberDTO mDto = new SplitMemberDTO();
            mDto.setId(m.getId());
            mDto.setName(m.getName());
            mDto.setEmailOrPhone(m.getEmailOrPhone());
            mDto.setCurrentUser(m.isCurrentUser());
            mDto.setGroupId(groupId);
            mDto.setTotalPaid(paid);
            mDto.setTotalOwed(owed);
            mDto.setNetBalance(net);
            memberBalances.add(mDto);

            if (m.isCurrentUser()) {
                currentUserNet = net;
                currentUserPaid = paid;
                currentUserShare = owed;
            }
        }

        // Min-Cash-Flow Debt Simplification Algorithm
        List<DebtTransferDTO> simplifiedTransfers = calculateSimplifiedDebts(members, netMap);

        SplitBalanceSummaryDTO summary = new SplitBalanceSummaryDTO();
        summary.setGroupId(groupId);
        summary.setGroupName(group.getName());
        summary.setTotalGroupSpending(totalGroupSpending.setScale(2, RoundingMode.HALF_UP));
        summary.setUserTotalPaid(currentUserPaid);
        summary.setUserTotalShare(currentUserShare);
        summary.setUserNetBalance(currentUserNet);

        if (currentUserNet.compareTo(new BigDecimal("0.01")) > 0) {
            summary.setUserStatusHeadline("You are owed ₹" + currentUserNet.toPlainString());
        } else if (currentUserNet.compareTo(new BigDecimal("-0.01")) < 0) {
            summary.setUserStatusHeadline("You owe ₹" + currentUserNet.abs().toPlainString());
        } else {
            summary.setUserStatusHeadline("All settled up");
        }

        summary.setMemberBalances(memberBalances);
        summary.setSimplifiedDebts(simplifiedTransfers);

        return summary;
    }

    // ==========================================
    // Internal Helper Methods
    // ==========================================

    private void verifyGroupOwnership(Long groupId, Long userId) {
        if (!groupRepository.findByIdAndUserId(groupId, userId).isPresent()) {
            throw new IllegalArgumentException("Split group not found or unauthorized");
        }
    }

    private List<SplitExpenseShare> computeShares(SplitExpense expense, SplitGroup group, SplitExpenseRequest request) {
        List<SplitMember> allGroupMembers = memberRepository.findByGroupIdOrderByIdAsc(group.getId());
        if (allGroupMembers.isEmpty()) {
            throw new IllegalArgumentException("Cannot create expense in group with no members");
        }

        List<SplitExpenseShare> shares = new ArrayList<>();
        BigDecimal total = expense.getTotalAmount();
        String method = expense.getSplitMethod();

        if ("EQUAL".equalsIgnoreCase(method)) {
            // Equal split among specified members in request or all members in group
            List<SplitMember> targetMembers = new ArrayList<>();
            if (request.getShares() != null && !request.getShares().isEmpty()) {
                Set<Long> requestedMemberIds = new HashSet<>();
                for (SplitExpenseShareRequest sr : request.getShares()) {
                    requestedMemberIds.add(sr.getMemberId());
                }
                for (SplitMember m : allGroupMembers) {
                    if (requestedMemberIds.contains(m.getId())) {
                        targetMembers.add(m);
                    }
                }
            }
            if (targetMembers.isEmpty()) {
                targetMembers = allGroupMembers;
            }

            int n = targetMembers.size();
            BigDecimal baseShare = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.FLOOR);
            BigDecimal distributed = baseShare.multiply(BigDecimal.valueOf(n));
            BigDecimal remainder = total.subtract(distributed);

            int remainderCents = remainder.multiply(BigDecimal.valueOf(100)).intValue();
            for (int i = 0; i < n; i++) {
                SplitMember m = targetMembers.get(i);
                BigDecimal memberShare = baseShare;
                if (i < remainderCents) {
                    memberShare = memberShare.add(new BigDecimal("0.01"));
                }
                shares.add(new SplitExpenseShare(expense, m, memberShare, null));
            }
        } else if ("EXACT".equalsIgnoreCase(method)) {
            if (request.getShares() == null || request.getShares().isEmpty()) {
                throw new IllegalArgumentException("Shares must be provided for exact split");
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (SplitExpenseShareRequest sr : request.getShares()) {
                SplitMember m = memberRepository.findByIdAndGroupId(sr.getMemberId(), group.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Member " + sr.getMemberId() + " not found in group"));
                BigDecimal amount = sr.getOwedAmount() != null ? sr.getOwedAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                sum = sum.add(amount);
                shares.add(new SplitExpenseShare(expense, m, amount, null));
            }
            if (sum.subtract(total).abs().compareTo(new BigDecimal("0.05")) > 0) {
                throw new IllegalArgumentException("Sum of individual shares (₹" + sum + ") does not match total expense amount (₹" + total + ")");
            }
        } else if ("PERCENT".equalsIgnoreCase(method)) {
            if (request.getShares() == null || request.getShares().isEmpty()) {
                throw new IllegalArgumentException("Shares with percentages must be provided");
            }
            double totalPercent = 0.0;
            BigDecimal sumCalculated = BigDecimal.ZERO;

            for (SplitExpenseShareRequest sr : request.getShares()) {
                SplitMember m = memberRepository.findByIdAndGroupId(sr.getMemberId(), group.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Member not found in group"));
                double pct = sr.getPercentage() != null ? sr.getPercentage() : 0.0;
                totalPercent += pct;

                BigDecimal memberOwed = total.multiply(BigDecimal.valueOf(pct / 100.0)).setScale(2, RoundingMode.HALF_UP);
                sumCalculated = sumCalculated.add(memberOwed);
                shares.add(new SplitExpenseShare(expense, m, memberOwed, pct));
            }

            // Adjust any cent rounding discrepancy to first share
            if (!shares.isEmpty() && sumCalculated.compareTo(total) != 0) {
                BigDecimal diff = total.subtract(sumCalculated);
                shares.get(0).setOwedAmount(shares.get(0).getOwedAmount().add(diff));
            }
        }

        return shares;
    }

    private List<DebtTransferDTO> calculateSimplifiedDebts(List<SplitMember> members, Map<Long, BigDecimal> netMap) {
        class BalanceNode {
            final SplitMember member;
            BigDecimal amount;

            BalanceNode(SplitMember member, BigDecimal amount) {
                this.member = member;
                this.amount = amount;
            }
        }

        List<BalanceNode> debtors = new ArrayList<>();   // net < 0 (owes money)
        List<BalanceNode> creditors = new ArrayList<>(); // net > 0 (is owed money)

        for (SplitMember m : members) {
            BigDecimal net = netMap.getOrDefault(m.getId(), BigDecimal.ZERO);
            if (net.compareTo(new BigDecimal("-0.01")) < 0) {
                debtors.add(new BalanceNode(m, net.abs()));
            } else if (net.compareTo(new BigDecimal("0.01")) > 0) {
                creditors.add(new BalanceNode(m, net));
            }
        }

        List<DebtTransferDTO> transfers = new ArrayList<>();

        int dIdx = 0;
        int cIdx = 0;

        while (dIdx < debtors.size() && cIdx < creditors.size()) {
            BalanceNode debtor = debtors.get(dIdx);
            BalanceNode creditor = creditors.get(cIdx);

            BigDecimal settleAmount = debtor.amount.min(creditor.amount).setScale(2, RoundingMode.HALF_UP);

            if (settleAmount.compareTo(BigDecimal.ZERO) > 0) {
                String debtorName = debtor.member.isCurrentUser() ? "You" : debtor.member.getName();
                String creditorName = creditor.member.isCurrentUser() ? "You" : creditor.member.getName();
                String narrative = debtorName + " pays " + creditorName + " ₹" + settleAmount.toPlainString();

                transfers.add(new DebtTransferDTO(
                        debtor.member.getId(),
                        debtor.member.getName(),
                        debtor.member.isCurrentUser(),
                        creditor.member.getId(),
                        creditor.member.getName(),
                        creditor.member.isCurrentUser(),
                        settleAmount,
                        narrative
                ));
            }

            debtor.amount = debtor.amount.subtract(settleAmount);
            creditor.amount = creditor.amount.subtract(settleAmount);

            if (debtor.amount.compareTo(new BigDecimal("0.01")) < 0) {
                dIdx++;
            }
            if (creditor.amount.compareTo(new BigDecimal("0.01")) < 0) {
                cIdx++;
            }
        }

        return transfers;
    }

    private SplitGroupDTO mapToGroupDTO(SplitGroup g, Long userId) {
        SplitGroupDTO dto = new SplitGroupDTO();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setDescription(g.getDescription());
        dto.setGroupType(g.getGroupType());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());

        List<SplitMember> members = memberRepository.findByGroupIdOrderByIdAsc(g.getId());
        dto.setMemberCount(members.size());

        List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDescCreatedAtDesc(g.getId());
        dto.setExpenseCount(expenses.size());

        // Quick calculation of current user's net balance in this group
        try {
            SplitBalanceSummaryDTO summary = getBalanceSummary(g.getId(), userId);
            dto.setNetBalanceForCurrentUser(summary.getUserNetBalance());
            dto.setMembers(summary.getMemberBalances());
        } catch (Exception e) {
            dto.setNetBalanceForCurrentUser(BigDecimal.ZERO);
        }

        return dto;
    }

    private SplitMemberDTO mapToMemberDTO(SplitMember m) {
        SplitMemberDTO dto = new SplitMemberDTO();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setEmailOrPhone(m.getEmailOrPhone());
        dto.setCurrentUser(m.isCurrentUser());
        dto.setGroupId(m.getGroup().getId());
        return dto;
    }

    private SplitExpenseResponse mapToExpenseResponse(SplitExpense e, Long groupId) {
        SplitExpenseResponse res = new SplitExpenseResponse();
        res.setId(e.getId());
        res.setTitle(e.getTitle());
        res.setTotalAmount(e.getTotalAmount());
        res.setExpenseDate(e.getExpenseDate());
        res.setGroupId(groupId);
        res.setGroupName(e.getGroup().getName());
        res.setPaidByMemberId(e.getPaidBy().getId());
        res.setPaidByMemberName(e.getPaidBy().getName());
        res.setPaidByCurrentUser(e.getPaidBy().isCurrentUser());
        res.setSplitMethod(e.getSplitMethod());
        res.setSyncedToLedger(e.isSyncedToLedger());
        res.setLedgerTransactionId(e.getLedgerTransactionId());
        res.setNotes(e.getNotes());
        res.setCreatedAt(e.getCreatedAt());

        if (e.getCategory() != null) {
            res.setCategoryId(e.getCategory().getId());
            res.setCategoryName(e.getCategory().getName());
        }

        List<SplitExpenseShareDTO> shareDTOs = new ArrayList<>();
        BigDecimal userShare = BigDecimal.ZERO;

        for (SplitExpenseShare s : e.getShares()) {
            SplitExpenseShareDTO sDto = new SplitExpenseShareDTO();
            sDto.setId(s.getId());
            sDto.setMemberId(s.getMember().getId());
            sDto.setMemberName(s.getMember().getName());
            sDto.setCurrentUser(s.getMember().isCurrentUser());
            sDto.setOwedAmount(s.getOwedAmount());
            sDto.setPercentage(s.getPercentage());
            shareDTOs.add(sDto);

            if (s.getMember().isCurrentUser()) {
                userShare = s.getOwedAmount();
            }
        }
        res.setShares(shareDTOs);
        res.setUserShareAmount(userShare);

        // Status text & net effect for user
        if (e.getPaidBy().isCurrentUser()) {
            BigDecimal lent = e.getTotalAmount().subtract(userShare);
            res.setUserNetEffect(lent);
            if (lent.compareTo(BigDecimal.ZERO) > 0) {
                res.setStatusText("You paid ₹" + e.getTotalAmount().toPlainString() + ", lent ₹" + lent.toPlainString());
            } else {
                res.setStatusText("You paid ₹" + e.getTotalAmount().toPlainString());
            }
        } else {
            res.setUserNetEffect(userShare.negate());
            if (userShare.compareTo(BigDecimal.ZERO) > 0) {
                res.setStatusText(e.getPaidBy().getName() + " paid, you owe ₹" + userShare.toPlainString());
            } else {
                res.setStatusText(e.getPaidBy().getName() + " paid ₹" + e.getTotalAmount().toPlainString() + " (not involved)");
            }
        }

        return res;
    }

    private SplitSettlementResponse mapToSettlementResponse(SplitSettlement s) {
        SplitSettlementResponse res = new SplitSettlementResponse();
        res.setId(s.getId());
        res.setGroupId(s.getGroup().getId());
        res.setGroupName(s.getGroup().getName());
        res.setPaidByMemberId(s.getPaidBy().getId());
        res.setPaidByMemberName(s.getPaidBy().getName());
        res.setPaidByCurrentUser(s.getPaidBy().isCurrentUser());
        res.setPaidToMemberId(s.getPaidTo().getId());
        res.setPaidToMemberName(s.getPaidTo().getName());
        res.setPaidToCurrentUser(s.getPaidTo().isCurrentUser());
        res.setAmount(s.getAmount());
        res.setSettlementDate(s.getSettlementDate());
        res.setNotes(s.getNotes());
        res.setRecordedInLedger(s.isRecordedInLedger());
        res.setLedgerTransactionId(s.getLedgerTransactionId());
        res.setCreatedAt(s.getCreatedAt());

        String payerName = s.getPaidBy().isCurrentUser() ? "You" : s.getPaidBy().getName();
        String receiverName = s.getPaidTo().isCurrentUser() ? "You" : s.getPaidTo().getName();
        res.setNarrative(payerName + " paid " + receiverName + " ₹" + s.getAmount().toPlainString());

        return res;
    }

    @Transactional(readOnly = true)
    public List<SplitContactDTO> getContacts(Long userId) {
        List<SplitMember> allMembers = memberRepository.findByUserId(userId);
        Map<String, SplitContactDTO> contactsMap = new LinkedHashMap<>();

        for (SplitMember m : allMembers) {
            if (m.isCurrentUser()) {
                continue;
            }
            String rawName = m.getName() != null ? m.getName().trim() : "";
            if (rawName.isEmpty() || rawName.equalsIgnoreCase("you")) {
                continue;
            }
            String key = rawName.toLowerCase();
            if (contactsMap.containsKey(key)) {
                SplitContactDTO existing = contactsMap.get(key);
                existing.setGroupsCount(existing.getGroupsCount() + 1);
                if ((existing.getPhoneNumber() == null || existing.getPhoneNumber().isBlank()) && m.getEmailOrPhone() != null) {
                    existing.setPhoneNumber(m.getEmailOrPhone());
                }
            } else {
                SplitContactDTO contact = new SplitContactDTO(
                        rawName,
                        m.getEmailOrPhone(),
                        null,
                        null,
                        null,
                        1
                );
                contactsMap.put(key, contact);
            }
        }

        // Cross-reference with registered users to enrich phone number, email and registered ID
        for (SplitContactDTO c : contactsMap.values()) {
            if (c.getPhoneNumber() != null && !c.getPhoneNumber().isBlank()) {
                userRepository.findByPhoneNumber(c.getPhoneNumber()).ifPresent(u -> {
                    c.setRegisteredUserId(u.getId());
                    if (c.getEmail() == null) {
                        c.setEmail(u.getEmail());
                    }
                });
            }
        }

        return new ArrayList<>(contactsMap.values());
    }

    @Transactional(readOnly = true)
    public List<SplitContactDTO> searchRegisteredUsers(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        return userRepository.searchByNameOrPhone(query.trim()).stream()
                .map(u -> new SplitContactDTO(
                        u.getName(),
                        u.getPhoneNumber(),
                        u.getEmail(),
                        null,
                        u.getId(),
                        0
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
