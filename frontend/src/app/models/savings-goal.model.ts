export interface SavingsGoal {
  id: number;
  name: string;
  targetAmount: number;
  currentAmount: number;
  remainingAmount: number;
  progressPercentage: number;
  targetDate?: string;
  isCompleted: boolean;
  completedAt?: string;
  notes?: string;
  accountId?: number;
  accountName?: string;
  userId: number;
  createdAt: string;
  updatedAt: string;
}

export interface SavingsGoalRequest {
  name: string;
  targetAmount: number;
  initialAmount?: number;
  targetDate?: string;
  notes?: string;
  accountId?: number;
  userId: number;
}

export interface GoalContribution {
  id: number;
  savingsGoalId: number;
  savingsGoalName: string;
  amount: number;
  contributionDate: string;
  notes?: string;
  sourceAccountId?: number;
  sourceAccountName?: string;
  userId: number;
  createdAt: string;
}

export interface GoalContributionRequest {
  amount: number;
  contributionDate?: string;
  notes?: string;
  sourceAccountId?: number;
  userId: number;
}
