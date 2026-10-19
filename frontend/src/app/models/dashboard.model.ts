import { Transaction } from './transaction.model';
import { SalaryPlan } from './salary-plan.model';
import { SavingsGoal } from './savings-goal.model';

export interface CategoryExpense {
  categoryName: string;
  amount: number;
  percentage: number;
}

export interface DashboardData {
  netWorth?: number;
  totalLiquidBalance?: number;
  totalIncome: number;
  totalExpenses: number;
  balance: number;
  thisMonthExpenses: number;
  accounts?: any[];
  salaryPlan?: SalaryPlan;
  emergencyFundGoal?: SavingsGoal;
  totalGoalsCount: number;
  totalGoalsTarget: number;
  totalGoalsSaved: number;
  overallGoalsProgress: number;
  recentTransactions: Transaction[];
  categoryExpenses: CategoryExpense[];
}
