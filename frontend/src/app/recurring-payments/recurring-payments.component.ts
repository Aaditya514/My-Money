import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RecurringPaymentService } from '../services/recurring-payment.service';
import { CategoryService } from '../services/category.service';
import { AuthService } from '../services/auth.service';
import { RecurringPayment, RecurrenceFrequency } from '../models/recurring-payment.model';
import { Category } from '../models/category.model';
import { TransactionType } from '../models/transaction.model';
import { DialogService } from '../services/dialog.service';

@Component({
  selector: 'app-recurring-payments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './recurring-payments.component.html',
  styleUrls: ['./recurring-payments.component.css']
})
export class RecurringPaymentsComponent implements OnInit {
  recurringPayments: RecurringPayment[] = [];
  categories: Category[] = [];
  loading: boolean = true;
  actionLoading: boolean = false;
  recordingId: number | null = null;
  errorMessage: string = '';
  successMessage: string = '';

  // Filter & Search
  activeFilter: 'ALL' | 'ACTIVE' | 'PAUSED' | 'DUE_SOON' = 'ALL';
  searchQuery: string = '';

  // Modal State
  showModal: boolean = false;
  isEditing: boolean = false;
  editingId: number | null = null;
  paymentForm: FormGroup;

  frequencies: { value: RecurrenceFrequency; label: string }[] = [
    { value: 'MONTHLY', label: 'Monthly (e.g. Rent, Bills, Subscriptions)' },
    { value: 'WEEKLY', label: 'Weekly' },
    { value: 'BI_WEEKLY', label: 'Bi-Weekly (Every 2 weeks)' },
    { value: 'QUARTERLY', label: 'Quarterly (Every 3 months)' },
    { value: 'YEARLY', label: 'Yearly (Annual bills, Insurance)' },
    { value: 'DAILY', label: 'Daily' }
  ];

  constructor(
    private fb: FormBuilder,
    private recurringService: RecurringPaymentService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private dialogService: DialogService
  ) {
    const today = new Date().toISOString().split('T')[0];
    this.paymentForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      type: ['EXPENSE', [Validators.required]],
      categoryId: [null, [Validators.required]],
      frequency: ['MONTHLY', [Validators.required]],
      startDate: [today, [Validators.required]],
      nextDueDate: [today, [Validators.required]],
      notes: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    const userId = this.authService.getUserId();

    this.categoryService.getCategories(undefined, userId).subscribe({
      next: (cats) => {
        this.categories = cats;
      },
      error: () => {
        // Non-blocking fallback
      }
    });

    this.recurringService.getRecurringPaymentsByUserId(userId).subscribe({
      next: (data) => {
        this.recurringPayments = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load repeating payments.';
      }
    });
  }

  get filteredPayments(): RecurringPayment[] {
    return this.recurringPayments.filter(p => {
      // Filter by status
      if (this.activeFilter === 'ACTIVE' && !p.active) return false;
      if (this.activeFilter === 'PAUSED' && p.active) return false;
      if (this.activeFilter === 'DUE_SOON') {
        if (!p.active) return false;
        if (p.daysUntilDue === undefined || p.daysUntilDue > 7) return false;
      }

      // Filter by search query
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        const matchesTitle = p.title.toLowerCase().includes(query);
        const matchesCategory = (p.categoryName || '').toLowerCase().includes(query);
        const matchesNotes = (p.notes || '').toLowerCase().includes(query);
        if (!matchesTitle && !matchesCategory && !matchesNotes) return false;
      }

      return true;
    });
  }

  // Summary Metrics
  get totalMonthlyCommitment(): number {
    return this.recurringPayments
      .filter(p => p.active && p.type === 'EXPENSE')
      .reduce((sum, p) => sum + this.getMonthlyEquivalent(p.amount, p.frequency), 0);
  }

  get activeCount(): number {
    return this.recurringPayments.filter(p => p.active).length;
  }

  get dueSoonCount(): number {
    return this.recurringPayments.filter(p => p.active && p.daysUntilDue !== undefined && p.daysUntilDue <= 7).length;
  }

  get dueSoonAmount(): number {
    return this.recurringPayments
      .filter(p => p.active && p.daysUntilDue !== undefined && p.daysUntilDue <= 7 && p.type === 'EXPENSE')
      .reduce((sum, p) => sum + p.amount, 0);
  }

  private getMonthlyEquivalent(amount: number, frequency: RecurrenceFrequency): number {
    switch (frequency) {
      case 'DAILY': return amount * 30;
      case 'WEEKLY': return amount * 4.33;
      case 'BI_WEEKLY': return amount * 2.16;
      case 'MONTHLY': return amount;
      case 'QUARTERLY': return amount / 3;
      case 'YEARLY': return amount / 12;
      default: return amount;
    }
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.editingId = null;
    const today = new Date().toISOString().split('T')[0];
    
    // Default to first expense category if available
    const defaultExpenseCat = this.categories.find(c => c.type === 'EXPENSE');

    this.paymentForm.reset({
      title: '',
      amount: null,
      type: 'EXPENSE',
      categoryId: defaultExpenseCat ? defaultExpenseCat.id : (this.categories[0]?.id || null),
      frequency: 'MONTHLY',
      startDate: today,
      nextDueDate: today,
      notes: '',
      active: true
    });
    this.showModal = true;
  }

  openEditModal(payment: RecurringPayment): void {
    this.isEditing = true;
    this.editingId = payment.id;
    this.paymentForm.patchValue({
      title: payment.title,
      amount: payment.amount,
      type: payment.type,
      categoryId: payment.categoryId,
      frequency: payment.frequency,
      startDate: payment.startDate,
      nextDueDate: payment.nextDueDate,
      notes: payment.notes || '',
      active: payment.active
    });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.isEditing = false;
    this.editingId = null;
    this.paymentForm.reset();
  }

  onSave(): void {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    this.actionLoading = true;
    const formVal = this.paymentForm.value;
    const userId = this.authService.getUserId();

    const requestPayload = {
      title: formVal.title.trim(),
      amount: Number(formVal.amount),
      type: formVal.type as TransactionType,
      categoryId: Number(formVal.categoryId),
      frequency: formVal.frequency as RecurrenceFrequency,
      startDate: formVal.startDate,
      nextDueDate: formVal.nextDueDate,
      notes: formVal.notes ? formVal.notes.trim() : '',
      userId: userId,
      active: formVal.active !== false
    };

    if (this.isEditing && this.editingId) {
      this.recurringService.updateRecurringPayment(this.editingId, requestPayload, userId).subscribe({
        next: (updated) => {
          this.actionLoading = false;
          this.closeModal();
          this.showSuccess(`Updated repeating payment '${updated.title}'.`);
          this.loadData();
        },
        error: (err) => {
          this.actionLoading = false;
          this.errorMessage = err.error?.message || 'Failed to update repeating payment.';
        }
      });
    } else {
      this.recurringService.createRecurringPayment(requestPayload).subscribe({
        next: (created) => {
          this.actionLoading = false;
          this.closeModal();
          this.showSuccess(`Created repeating payment '${created.title}'.`);
          this.loadData();
        },
        error: (err) => {
          this.actionLoading = false;
          this.errorMessage = err.error?.message || 'Failed to create repeating payment.';
        }
      });
    }
  }

  onToggleActive(payment: RecurringPayment): void {
    const userId = this.authService.getUserId();
    this.recurringService.toggleActive(payment.id, userId).subscribe({
      next: (updated) => {
        payment.active = updated.active;
        this.showSuccess(`Payment '${payment.title}' is now ${updated.active ? 'Active' : 'Paused'}.`);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to update status.';
      }
    });
  }

  onRecordPayment(payment: RecurringPayment): void {
    if (this.recordingId) return;
    this.recordingId = payment.id;
    const userId = this.authService.getUserId();

    this.recurringService.recordPayment(payment.id, userId).subscribe({
      next: (result) => {
        this.recordingId = null;
        this.showSuccess(result.message || `Recorded ₹${payment.amount} payment for '${payment.title}'!`);
        this.loadData();
      },
      error: (err) => {
        this.recordingId = null;
        this.errorMessage = err.error?.message || 'Failed to record payment.';
      }
    });
  }

  async onDelete(payment: RecurringPayment): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Repeating Payment',
      message: `Are you sure you want to delete repeating payment '${payment.title}'? This will not delete previously recorded transactions.`,
      confirmText: 'Delete Payment',
      cancelText: 'Keep Payment',
      type: 'danger',
      icon: 'trash'
    });

    if (!confirmed) {
      return;
    }

    const userId = this.authService.getUserId();
    this.recurringService.deleteRecurringPayment(payment.id, userId).subscribe({
      next: () => {
        this.showSuccess(`Deleted '${payment.title}'.`);
        this.loadData();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to delete repeating payment.';
      }
    });
  }

  showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => {
      if (this.successMessage === msg) {
        this.successMessage = '';
      }
    }, 4500);
  }

  getDueBadgeClass(payment: RecurringPayment): string {
    if (!payment.active) return 'badge-paused';
    if (payment.overdue) return 'badge-overdue';
    if (payment.daysUntilDue !== undefined && payment.daysUntilDue === 0) return 'badge-today';
    if (payment.daysUntilDue !== undefined && payment.daysUntilDue <= 3) return 'badge-urgent';
    if (payment.daysUntilDue !== undefined && payment.daysUntilDue <= 7) return 'badge-soon';
    return 'badge-normal';
  }

  getDueText(payment: RecurringPayment): string {
    if (!payment.active) return 'Paused';
    if (payment.overdue) {
      const days = Math.abs(payment.daysUntilDue || 0);
      return `Overdue by ${days} ${days === 1 ? 'day' : 'days'}`;
    }
    if (payment.daysUntilDue === 0) return 'Due Today';
    if (payment.daysUntilDue === 1) return 'Due Tomorrow';
    if (payment.daysUntilDue !== undefined && payment.daysUntilDue <= 7) {
      return `Due in ${payment.daysUntilDue} days`;
    }
    return `Due on ${payment.nextDueDate}`;
  }

  getCategoryIcon(name?: string): string {
    if (!name) return '📝';
    const n = name.toLowerCase();
    if (n.includes('rent') || n.includes('housing')) return '🏠';
    if (n.includes('util') || n.includes('bill') || n.includes('electric') || n.includes('water')) return '💡';
    if (n.includes('internet') || n.includes('wifi') || n.includes('phone') || n.includes('broadband')) return '🌐';
    if (n.includes('entertain') || n.includes('stream') || n.includes('netflix') || n.includes('music')) return '🎬';
    if (n.includes('food') || n.includes('grocer')) return '🛒';
    if (n.includes('insur') || n.includes('health') || n.includes('medic')) return '🛡️';
    if (n.includes('gym') || n.includes('fit')) return '💪';
    if (n.includes('salary') || n.includes('income')) return '💰';
    return '📋';
  }
}
