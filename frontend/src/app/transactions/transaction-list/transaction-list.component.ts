import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TransactionService } from '../../services/transaction.service';
import { CategoryService } from '../../services/category.service';
import { AuthService } from '../../services/auth.service';
import { Transaction } from '../../models/transaction.model';
import { Category } from '../../models/category.model';
import { DialogService } from '../../services/dialog.service';
import { ExportService } from '../../services/export.service';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent implements OnInit {
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  categories: Category[] = [];

  // Filter States
  searchTerm: string = '';
  selectedType: string = 'ALL';
  selectedCategoryId: string = 'ALL';

  // Date Presets & Custom Range
  datePreset: 'ALL' | 'THIS_MONTH' | 'LAST_30_DAYS' | 'THIS_YEAR' | 'CUSTOM' = 'ALL';
  customStartDate: string = '';
  customEndDate: string = '';

  loading: boolean = true;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private exportService: ExportService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadTransactions();
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => (this.errorMessage = 'Could not load categories.')
    });
  }

  loadTransactions(): void {
    const userId = this.authService.getUserId();

    this.loading = true;
    this.transactionService.getTransactionsByUserId(userId).subscribe({
      next: (data) => {
        this.transactions = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not load transactions.';
      }
    });
  }

  setPreset(preset: 'ALL' | 'THIS_MONTH' | 'LAST_30_DAYS' | 'THIS_YEAR' | 'CUSTOM'): void {
    this.datePreset = preset;
    const now = new Date();

    if (preset === 'ALL') {
      this.customStartDate = '';
      this.customEndDate = '';
    } else if (preset === 'THIS_MONTH') {
      const year = now.getFullYear();
      const month = now.getMonth();
      const firstDay = new Date(year, month, 1);
      const lastDay = new Date(year, month + 1, 0);
      this.customStartDate = this.formatDate(firstDay);
      this.customEndDate = this.formatDate(lastDay);
    } else if (preset === 'LAST_30_DAYS') {
      const past30 = new Date();
      past30.setDate(past30.getDate() - 30);
      this.customStartDate = this.formatDate(past30);
      this.customEndDate = this.formatDate(now);
    } else if (preset === 'THIS_YEAR') {
      const firstDay = new Date(now.getFullYear(), 0, 1);
      const lastDay = new Date(now.getFullYear(), 11, 31);
      this.customStartDate = this.formatDate(firstDay);
      this.customEndDate = this.formatDate(lastDay);
    }
    this.applyFilters();
  }

  private formatDate(d: Date): string {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  applyFilters(): void {
    let result = this.transactions;

    // Filter by type
    if (this.selectedType !== 'ALL') {
      result = result.filter((t) => t.type === this.selectedType);
    }

    // Filter by category
    if (this.selectedCategoryId !== 'ALL') {
      const catId = Number(this.selectedCategoryId);
      result = result.filter((t) => t.categoryId === catId);
    }

    // Filter by date range
    if (this.customStartDate) {
      result = result.filter((t) => t.transactionDate >= this.customStartDate);
    }
    if (this.customEndDate) {
      result = result.filter((t) => t.transactionDate <= this.customEndDate);
    }

    // Filter by search text
    if (this.searchTerm.trim() !== '') {
      const search = this.searchTerm.toLowerCase().trim();
      result = result.filter(
        (t) =>
          t.title.toLowerCase().includes(search) ||
          (t.description && t.description.toLowerCase().includes(search))
      );
    }

    this.filteredTransactions = result;
  }

  // Summary Metrics for filtered transactions
  getTotalIncome(): number {
    return this.filteredTransactions
      .filter((t) => t.type === 'INCOME' || t.type === 'REFUND')
      .reduce((sum, t) => sum + Number(t.amount || 0), 0);
  }

  getTotalExpense(): number {
    return this.filteredTransactions
      .filter((t) => t.type === 'EXPENSE')
      .reduce((sum, t) => sum + Number(t.amount || 0), 0);
  }

  getNetBalance(): number {
    return this.getTotalIncome() - this.getTotalExpense();
  }

  exportCsv(): void {
    const userId = this.authService.getUserId();
    this.exportService.downloadTransactionsCsv(userId, this.customStartDate, this.customEndDate);
  }

  printStatement(): void {
    this.exportService.printCurrentPage();
  }

  async onDelete(id: number): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Transaction',
      message: 'Are you sure you want to delete this transaction from your records?',
      confirmText: 'Delete Transaction',
      cancelText: 'Cancel',
      type: 'danger',
      icon: 'trash'
    });

    if (confirmed) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: () => {
          this.successMessage = 'Transaction deleted successfully.';
          this.loadTransactions();
          setTimeout(() => (this.successMessage = ''), 4000);
        },
        error: () => {
          this.errorMessage = 'Failed to delete transaction.';
        }
      });
    }
  }
}

