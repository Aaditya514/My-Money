export type SalaryPlanAllocationType = 'EXPENSE' | 'SAVINGS' | 'OTHER';

export interface SalaryPlanIncomeSource {
  id?: number;
  sourceName: string;
  plannedAmount: number;
  orderIndex?: number;
}

export interface SalaryPlanAllocation {
  id?: number;
  name: string;
  plannedAmount: number;
  allocationType: SalaryPlanAllocationType;
  categoryId?: number;
  orderIndex?: number;
}

export interface SalaryPlan {
  id?: number;
  month: number;
  year: number;
  isConfigured: boolean;
  configured?: boolean;

  // Planned Totals
  totalPlannedIncome: number;
  totalPlannedExpenses: number;
  totalPlannedSavings: number;
  totalPlannedOther: number;
  totalPlannedAllocations: number;
  unallocatedCarryForward: number;
  calculatedBuffer: number;
  isOverAllocated: boolean;
  overAllocated?: boolean;
  overAllocatedAmount: number;

  // Itemized breakdown
  incomeSources: SalaryPlanIncomeSource[];
  allocations: SalaryPlanAllocation[];

  // Legacy fields for backward compatibility
  takeHomeSalary: number;
  plannedExpenses: number;
  plannedEmergencyFund: number;
  emergencyFundTarget?: number;
  initialEmergencyFundBalance?: number;

  // Actual Tracked Values
  actualIncome: number;
  actualExpenses: number;
  actualEmergencyFund: number;
  actualNetSavings: number;

  // Variances
  incomeVariance: number;
  expenseVariance: number;
  emergencyFundVariance: number;

  // Emergency Fund Progress (optional)
  emergencyFundTotalBalance?: number;
  emergencyFundProgressPercentage?: number;

  notes?: string;
  userId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface SalaryPlanRequest {
  month: number;
  year: number;
  takeHomeSalary?: number;
  totalPlannedIncome?: number;
  incomeSources?: SalaryPlanIncomeSource[];
  allocations?: SalaryPlanAllocation[];
  unallocatedCarryForward?: number;
  plannedExpenses?: number;
  plannedEmergencyFund?: number;
  emergencyFundTarget?: number;
  initialEmergencyFundBalance?: number;
  notes?: string;
  userId: number;
}

export interface SalaryPlanCopyRequest {
  fromMonth: number;
  fromYear: number;
  toMonth: number;
  toYear: number;
  carryForwardUnallocated: boolean;
  userId: number;
}
