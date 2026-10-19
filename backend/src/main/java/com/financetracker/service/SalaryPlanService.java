package com.financetracker.service;

import com.financetracker.dto.SalaryPlanAllocationDto;
import com.financetracker.dto.SalaryPlanCopyRequest;
import com.financetracker.dto.SalaryPlanIncomeSourceDto;
import com.financetracker.dto.SalaryPlanRequest;
import com.financetracker.dto.SalaryPlanResponse;
import com.financetracker.entity.SalaryPlan;
import com.financetracker.entity.SalaryPlanAllocation;
import com.financetracker.entity.SalaryPlanAllocationType;
import com.financetracker.entity.SalaryPlanIncomeSource;
import com.financetracker.entity.SavingsGoalContribution;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.TransactionType;
import com.financetracker.entity.User;
import com.financetracker.repository.SalaryPlanRepository;
import com.financetracker.repository.SavingsGoalContributionRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SalaryPlanService {

    private final SalaryPlanRepository salaryPlanRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalContributionRepository contributionRepository;
    private final UserRepository userRepository;

    public SalaryPlanService(SalaryPlanRepository salaryPlanRepository,
                             TransactionRepository transactionRepository,
                             SavingsGoalContributionRepository contributionRepository,
                             UserRepository userRepository) {
        this.salaryPlanRepository = salaryPlanRepository;
        this.transactionRepository = transactionRepository;
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
    }

    public SalaryPlanResponse getSalaryPlan(Long userId, int month, int year) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return SalaryPlanResponse.unconfigured(userId, month, year);
        }
        User user = userOpt.get();

        Optional<SalaryPlan> planOpt = salaryPlanRepository.findByUserIdAndMonthAndYear(user.getId(), month, year);

        if (planOpt.isEmpty()) {
            ActualsSummary actuals = computeActuals(user.getId(), month, year);
            return SalaryPlanResponse.unconfigured(
                    user.getId(),
                    month,
                    year,
                    actuals.actualIncome,
                    actuals.actualExpenses,
                    actuals.actualSavings,
                    actuals.actualNetSavings
            );
        }

        return buildResponse(planOpt.get(), user.getId(), month, year);
    }

    public SalaryPlanResponse saveSalaryPlan(SalaryPlanRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        Optional<SalaryPlan> existingOpt = salaryPlanRepository.findByUserIdAndMonthAndYear(
                user.getId(), request.getMonth(), request.getYear());

        SalaryPlan plan;
        if (existingOpt.isPresent()) {
            plan = existingOpt.get();
        } else {
            plan = new SalaryPlan();
            plan.setUser(user);
            plan.setMonth(request.getMonth());
            plan.setYear(request.getYear());
        }

        plan.setNotes(request.getNotes());
        plan.setUnallocatedCarryForward(
                request.getUnallocatedCarryForward() != null ? request.getUnallocatedCarryForward() : BigDecimal.ZERO);

        // Update Income Sources
        plan.getIncomeSources().clear();
        if (request.getIncomeSources() != null && !request.getIncomeSources().isEmpty()) {
            for (int i = 0; i < request.getIncomeSources().size(); i++) {
                SalaryPlanIncomeSourceDto dto = request.getIncomeSources().get(i);
                SalaryPlanIncomeSource src = new SalaryPlanIncomeSource(
                        dto.getSourceName() != null ? dto.getSourceName().trim() : "Income " + (i + 1),
                        dto.getPlannedAmount() != null ? dto.getPlannedAmount() : BigDecimal.ZERO,
                        dto.getOrderIndex() != 0 ? dto.getOrderIndex() : i,
                        plan
                );
                plan.getIncomeSources().add(src);
            }
        } else if (request.getTakeHomeSalary() != null) {
            SalaryPlanIncomeSource defaultSrc = new SalaryPlanIncomeSource(
                    "Primary Income",
                    request.getTakeHomeSalary(),
                    0,
                    plan
            );
            plan.getIncomeSources().add(defaultSrc);
        }

        // Update Allocations
        plan.getAllocations().clear();
        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            for (int i = 0; i < request.getAllocations().size(); i++) {
                SalaryPlanAllocationDto dto = request.getAllocations().get(i);
                SalaryPlanAllocation alloc = new SalaryPlanAllocation(
                        dto.getName() != null ? dto.getName().trim() : "Category " + (i + 1),
                        dto.getPlannedAmount() != null ? dto.getPlannedAmount() : BigDecimal.ZERO,
                        dto.getAllocationType() != null ? dto.getAllocationType() : SalaryPlanAllocationType.EXPENSE,
                        dto.getCategoryId(),
                        dto.getOrderIndex() != 0 ? dto.getOrderIndex() : i,
                        plan
                );
                plan.getAllocations().add(alloc);
            }
        } else {
            int order = 0;
            if (request.getPlannedExpenses() != null) {
                plan.getAllocations().add(new SalaryPlanAllocation(
                        "Living Expenses", request.getPlannedExpenses(), SalaryPlanAllocationType.EXPENSE, null, order++, plan));
            }
            if (request.getPlannedEmergencyFund() != null) {
                plan.getAllocations().add(new SalaryPlanAllocation(
                        "Emergency Reserve", request.getPlannedEmergencyFund(), SalaryPlanAllocationType.SAVINGS, null, order++, plan));
            }
        }

        // Sync summary columns
        plan.setTakeHomeSalary(plan.calculateTotalPlannedIncome());
        plan.setPlannedExpenses(plan.calculateTotalPlannedExpenses());
        plan.setPlannedEmergencyFund(plan.calculateTotalPlannedSavings());

        if (request.getEmergencyFundTarget() != null) {
            plan.setEmergencyFundTarget(request.getEmergencyFundTarget());
        }
        if (request.getInitialEmergencyFundBalance() != null) {
            plan.setInitialEmergencyFundBalance(request.getInitialEmergencyFundBalance());
        }

        SalaryPlan saved = salaryPlanRepository.save(plan);
        return buildResponse(saved, user.getId(), saved.getMonth(), saved.getYear());
    }

    public SalaryPlanResponse copySalaryPlan(SalaryPlanCopyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        SalaryPlan sourcePlan = salaryPlanRepository.findByUserIdAndMonthAndYear(
                user.getId(), request.getFromMonth(), request.getFromYear())
                .orElseThrow(() -> new RuntimeException("Source salary plan not found for " +
                        request.getFromMonth() + "/" + request.getFromYear()));

        Optional<SalaryPlan> targetOpt = salaryPlanRepository.findByUserIdAndMonthAndYear(
                user.getId(), request.getToMonth(), request.getToYear());

        SalaryPlan targetPlan = targetOpt.orElseGet(() -> {
            SalaryPlan np = new SalaryPlan();
            np.setUser(user);
            np.setMonth(request.getToMonth());
            np.setYear(request.getToYear());
            return np;
        });

        BigDecimal carryForward = BigDecimal.ZERO;
        if (request.isCarryForwardUnallocated()) {
            carryForward = sourcePlan.calculateRemainingBuffer();
        }
        targetPlan.setUnallocatedCarryForward(carryForward);
        targetPlan.setNotes("Copied from " + request.getFromMonth() + "/" + request.getFromYear());
        targetPlan.setEmergencyFundTarget(sourcePlan.getEmergencyFundTarget());
        targetPlan.setInitialEmergencyFundBalance(sourcePlan.getInitialEmergencyFundBalance());

        targetPlan.getIncomeSources().clear();
        for (SalaryPlanIncomeSource src : sourcePlan.getIncomeSources()) {
            targetPlan.getIncomeSources().add(new SalaryPlanIncomeSource(
                    src.getSourceName(), src.getPlannedAmount(), src.getOrderIndex(), targetPlan));
        }

        targetPlan.getAllocations().clear();
        for (SalaryPlanAllocation alloc : sourcePlan.getAllocations()) {
            targetPlan.getAllocations().add(new SalaryPlanAllocation(
                    alloc.getName(), alloc.getPlannedAmount(), alloc.getAllocationType(),
                    alloc.getCategoryId(), alloc.getOrderIndex(), targetPlan));
        }

        targetPlan.setTakeHomeSalary(sourcePlan.getTakeHomeSalary());
        targetPlan.setPlannedExpenses(sourcePlan.getPlannedExpenses());
        targetPlan.setPlannedEmergencyFund(sourcePlan.getPlannedEmergencyFund());

        SalaryPlan saved = salaryPlanRepository.save(targetPlan);
        return buildResponse(saved, user.getId(), targetPlan.getMonth(), targetPlan.getYear());
    }

    public void deleteSalaryPlan(Long id, Long userId) {
        SalaryPlan plan = salaryPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salary plan not found with id: " + id));
        if (userId != null && !plan.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Plan does not belong to user");
        }
        salaryPlanRepository.delete(plan);
    }

    public void deleteSalaryPlanByMonthYear(Long userId, int month, int year) {
        Optional<SalaryPlan> planOpt = salaryPlanRepository.findByUserIdAndMonthAndYear(userId, month, year);
        planOpt.ifPresent(salaryPlanRepository::delete);
    }

    private SalaryPlanResponse buildResponse(SalaryPlan plan, Long userId, int month, int year) {
        ActualsSummary actuals = computeActuals(userId, month, year);

        BigDecimal totalPlannedIncome = plan.calculateTotalPlannedIncome();
        BigDecimal totalPlannedExpenses = plan.calculateTotalPlannedExpenses();
        BigDecimal totalPlannedSavings = plan.calculateTotalPlannedSavings();
        BigDecimal totalPlannedOther = plan.calculateTotalPlannedOther();
        BigDecimal totalPlannedAllocations = plan.calculateTotalAllocations();
        BigDecimal unallocatedCarryForward = plan.getUnallocatedCarryForward() != null ?
                plan.getUnallocatedCarryForward() : BigDecimal.ZERO;
        BigDecimal calculatedBuffer = plan.calculateRemainingBuffer();

        BigDecimal incomeVariance = actuals.actualIncome.subtract(totalPlannedIncome);
        BigDecimal expenseVariance = totalPlannedExpenses.subtract(actuals.actualExpenses);
        BigDecimal emergencyFundVariance = actuals.actualSavings.subtract(totalPlannedSavings);

        List<SavingsGoalContribution> allContributions = contributionRepository
                .findByUserIdOrderByContributionDateDescCreatedAtDesc(userId);
        BigDecimal allTimeContributions = BigDecimal.ZERO;
        for (SavingsGoalContribution c : allContributions) {
            allTimeContributions = allTimeContributions.add(c.getAmount());
        }

        BigDecimal emergencyFundTotalBalance = (plan.getInitialEmergencyFundBalance() != null ?
                plan.getInitialEmergencyFundBalance() : BigDecimal.ZERO).add(allTimeContributions);
        double progressPercentage = 0.0;
        if (plan.getEmergencyFundTarget() != null && plan.getEmergencyFundTarget().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = emergencyFundTotalBalance
                    .divide(plan.getEmergencyFundTarget(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            if (progressPercentage > 100.0) {
                progressPercentage = 100.0;
            }
        }

        SalaryPlanResponse resp = new SalaryPlanResponse(
                plan.getId(),
                plan.getMonth(),
                plan.getYear(),
                totalPlannedIncome,
                totalPlannedExpenses,
                totalPlannedSavings,
                calculatedBuffer,
                plan.getEmergencyFundTarget(),
                plan.getInitialEmergencyFundBalance(),
                actuals.actualIncome,
                actuals.actualExpenses,
                actuals.actualSavings,
                actuals.actualNetSavings,
                incomeVariance,
                expenseVariance,
                emergencyFundVariance,
                emergencyFundTotalBalance,
                progressPercentage,
                plan.getNotes(),
                userId,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );

        resp.setConfigured(true);
        resp.setTotalPlannedOther(totalPlannedOther);
        resp.setTotalPlannedAllocations(totalPlannedAllocations);
        resp.setUnallocatedCarryForward(unallocatedCarryForward);

        // Map itemized income sources
        List<SalaryPlanIncomeSourceDto> incDtos = new ArrayList<>();
        if (plan.getIncomeSources() != null) {
            for (SalaryPlanIncomeSource src : plan.getIncomeSources()) {
                incDtos.add(new SalaryPlanIncomeSourceDto(
                        src.getId(), src.getSourceName(), src.getPlannedAmount(), src.getOrderIndex()));
            }
        }
        resp.setIncomeSources(incDtos);

        // Map itemized allocations
        List<SalaryPlanAllocationDto> allocDtos = new ArrayList<>();
        if (plan.getAllocations() != null) {
            for (SalaryPlanAllocation alloc : plan.getAllocations()) {
                allocDtos.add(new SalaryPlanAllocationDto(
                        alloc.getId(),
                        alloc.getName(),
                        alloc.getPlannedAmount(),
                        alloc.getAllocationType(),
                        alloc.getCategoryId(),
                        alloc.getOrderIndex()
                ));
            }
        }
        resp.setAllocations(allocDtos);

        return resp;
    }

    private ActualsSummary computeActuals(Long userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(userId, startOfMonth, endOfMonth);

        BigDecimal actualIncome = BigDecimal.ZERO;
        BigDecimal actualExpenses = BigDecimal.ZERO;

        for (Transaction t : monthTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                actualIncome = actualIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                actualExpenses = actualExpenses.add(t.getAmount());
            } else if (t.getType() == TransactionType.REFUND) {
                actualExpenses = actualExpenses.subtract(t.getAmount());
            }
        }

        List<SavingsGoalContribution> monthContributions = contributionRepository
                .findByUserIdAndContributionDateBetween(userId, startOfMonth, endOfMonth);

        BigDecimal actualSavings = BigDecimal.ZERO;
        for (SavingsGoalContribution c : monthContributions) {
            actualSavings = actualSavings.add(c.getAmount());
        }

        BigDecimal actualNetSavings = actualIncome
                .subtract(actualExpenses)
                .subtract(actualSavings);

        return new ActualsSummary(actualIncome, actualExpenses, actualSavings, actualNetSavings);
    }

    private static class ActualsSummary {
        final BigDecimal actualIncome;
        final BigDecimal actualExpenses;
        final BigDecimal actualSavings;
        final BigDecimal actualNetSavings;

        ActualsSummary(BigDecimal actualIncome, BigDecimal actualExpenses, BigDecimal actualSavings,
                       BigDecimal actualNetSavings) {
            this.actualIncome = actualIncome;
            this.actualExpenses = actualExpenses;
            this.actualSavings = actualSavings;
            this.actualNetSavings = actualNetSavings;
        }
    }
}
