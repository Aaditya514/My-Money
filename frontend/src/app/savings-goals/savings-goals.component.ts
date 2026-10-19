import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SavingsGoalService } from '../services/savings-goal.service';
import { AuthService } from '../services/auth.service';
import { SavingsGoal, GoalContribution, SavingsGoalRequest, GoalContributionRequest } from '../models/savings-goal.model';
import { DialogService } from '../services/dialog.service';

@Component({
  selector: 'app-savings-goals',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './savings-goals.component.html',
  styleUrls: ['./savings-goals.component.css']
})
export class SavingsGoalsComponent implements OnInit {
  goals: SavingsGoal[] = [];
  loading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  // Goal Form State
  showGoalModal: boolean = false;
  editingGoalId: number | null = null;
  goalForm: FormGroup;
  savingGoal: boolean = false;

  // Contribution Modal State
  showContributeModal: boolean = false;
  selectedGoalForContribution: SavingsGoal | null = null;
  contributeForm: FormGroup;
  contributing: boolean = false;

  // History Modal State
  showHistoryModal: boolean = false;
  selectedGoalForHistory: SavingsGoal | null = null;
  historyContributions: GoalContribution[] = [];
  loadingHistory: boolean = false;

  constructor(
    private fb: FormBuilder,
    private goalService: SavingsGoalService,
    private authService: AuthService,
    private dialogService: DialogService
  ) {
    this.goalForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(150)]],
      targetAmount: ['', [Validators.required, Validators.min(1)]],
      initialAmount: ['0', [Validators.min(0)]],
      targetDate: [''],
      notes: ['']
    });

    const today = new Date().toISOString().substring(0, 10);
    this.contributeForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(1)]],
      contributionDate: [today, [Validators.required]],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    const userId = this.authService.getUserId();
    this.loading = true;
    this.errorMessage = '';

    this.goalService.getGoals(userId).subscribe({
      next: (data) => {
        this.goals = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load savings goals.';
      }
    });
  }

  // Summary Metrics
  getTotalTarget(): number {
    return this.goals.reduce((acc, g) => acc + (g.targetAmount || 0), 0);
  }

  getTotalCurrent(): number {
    return this.goals.reduce((acc, g) => acc + (g.currentAmount || 0), 0);
  }

  getTotalProgress(): number {
    const target = this.getTotalTarget();
    if (target === 0) return 0;
    return Math.min(100, Math.round((this.getTotalCurrent() / target) * 100));
  }

  getCompletedGoalsCount(): number {
    return this.goals.filter(g => g.isCompleted).length;
  }

  // Modal Actions
  openCreateModal(): void {
    this.editingGoalId = null;
    this.goalForm.reset({
      name: '',
      targetAmount: '',
      initialAmount: '0',
      targetDate: '',
      notes: ''
    });
    this.errorMessage = '';
    this.showGoalModal = true;
  }

  openEditModal(goal: SavingsGoal): void {
    this.editingGoalId = goal.id;
    this.goalForm.patchValue({
      name: goal.name,
      targetAmount: goal.targetAmount,
      initialAmount: goal.currentAmount,
      targetDate: goal.targetDate || '',
      notes: goal.notes || ''
    });
    this.errorMessage = '';
    this.showGoalModal = true;
  }

  closeGoalModal(): void {
    this.showGoalModal = false;
    this.editingGoalId = null;
    this.savingGoal = false;
  }

  saveGoal(): void {
    if (this.goalForm.invalid) {
      this.goalForm.markAllAsTouched();
      return;
    }

    const userId = this.authService.getUserId();

    this.savingGoal = true;
    this.errorMessage = '';

    const formVal = this.goalForm.value;
    const req: SavingsGoalRequest = {
      name: formVal.name.trim(),
      targetAmount: Number(formVal.targetAmount),
      initialAmount: formVal.initialAmount ? Number(formVal.initialAmount) : 0,
      targetDate: formVal.targetDate || undefined,
      notes: formVal.notes ? formVal.notes.trim() : undefined,
      userId: userId
    };

    if (this.editingGoalId) {
      this.goalService.updateGoal(this.editingGoalId, req, userId).subscribe({
        next: (res) => {
          this.savingGoal = false;
          this.closeGoalModal();
          this.successMessage = `Savings goal "${res.name}" updated successfully.`;
          this.loadData();
          setTimeout(() => (this.successMessage = ''), 5000);
        },
        error: (err) => {
          this.savingGoal = false;
          this.errorMessage = err.error?.message || 'Failed to update savings goal.';
        }
      });
    } else {
      this.goalService.createGoal(req).subscribe({
        next: (res) => {
          this.savingGoal = false;
          this.closeGoalModal();
          this.successMessage = `Savings goal "${res.name}" created successfully!`;
          this.loadData();
          setTimeout(() => (this.successMessage = ''), 5000);
        },
        error: (err) => {
          this.savingGoal = false;
          this.errorMessage = err.error?.message || 'Failed to create savings goal.';
        }
      });
    }
  }

  async deleteGoal(goal: SavingsGoal): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Savings Goal',
      message: `Are you sure you want to delete goal "${goal.name}"? This action cannot be undone.`,
      confirmText: 'Delete Goal',
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'trash'
    });

    if (!confirmed) {
      return;
    }

    const userId = this.authService.getUserId();
    this.goalService.deleteGoal(goal.id, userId).subscribe({
      next: () => {
        this.successMessage = `Goal "${goal.name}" deleted successfully.`;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to delete goal.';
      }
    });
  }

  // Quick Inline Add Amount
  quickAddFunds(goal: SavingsGoal, amount: number): void {
    const userId = this.authService.getUserId();
    const req: GoalContributionRequest = {
      amount: amount,
      contributionDate: new Date().toISOString().substring(0, 10),
      notes: `Quick deposit of ₹${amount}`,
      userId: userId
    };

    this.goalService.contributeToGoal(goal.id, req, userId).subscribe({
      next: () => {
        this.successMessage = `Deposited ₹${amount.toLocaleString('en-IN')} to "${goal.name}".`;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Deposit failed.';
      }
    });
  }

  // Contribution Handling
  openContributeModal(goal: SavingsGoal): void {
    this.selectedGoalForContribution = goal;
    const today = new Date().toISOString().substring(0, 10);
    this.contributeForm.reset({
      amount: '',
      contributionDate: today,
      notes: ''
    });
    this.errorMessage = '';
    this.showContributeModal = true;
  }

  closeContributeModal(): void {
    this.showContributeModal = false;
    this.selectedGoalForContribution = null;
    this.contributing = false;
  }

  submitContribution(): void {
    if (this.contributeForm.invalid || !this.selectedGoalForContribution) {
      this.contributeForm.markAllAsTouched();
      return;
    }

    const userId = this.authService.getUserId();

    this.contributing = true;
    this.errorMessage = '';

    const formVal = this.contributeForm.value;
    const req: GoalContributionRequest = {
      amount: Number(formVal.amount),
      contributionDate: formVal.contributionDate,
      notes: formVal.notes ? formVal.notes.trim() : undefined,
      userId: userId
    };

    this.goalService.contributeToGoal(this.selectedGoalForContribution.id, req, userId).subscribe({
      next: (resp) => {
        this.contributing = false;
        this.closeContributeModal();
        this.successMessage = `Contributed ₹${resp.amount.toLocaleString('en-IN')} to "${this.selectedGoalForContribution?.name}".`;
        this.loadData();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.contributing = false;
        this.errorMessage = err.error?.message || 'Failed to submit contribution.';
      }
    });
  }

  // History Modal
  openHistoryModal(goal: SavingsGoal): void {
    this.selectedGoalForHistory = goal;
    this.historyContributions = [];
    this.loadingHistory = true;
    this.showHistoryModal = true;

    const userId = this.authService.getUserId();
    this.goalService.getContributions(goal.id, userId).subscribe({
      next: (data) => {
        this.historyContributions = data;
        this.loadingHistory = false;
      },
      error: () => {
        this.loadingHistory = false;
      }
    });
  }

  closeHistoryModal(): void {
    this.showHistoryModal = false;
    this.selectedGoalForHistory = null;
  }

  toggleComplete(goal: SavingsGoal): void {
    const userId = this.authService.getUserId();
    this.goalService.toggleComplete(goal.id, userId).subscribe({
      next: (updated: SavingsGoal) => {
        goal.isCompleted = updated.isCompleted;
        this.successMessage = updated.isCompleted
          ? `Goal "${goal.name}" marked as completed!`
          : `Goal "${goal.name}" reopened.`;
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || 'Failed to toggle goal status.';
      }
    });
  }
}

