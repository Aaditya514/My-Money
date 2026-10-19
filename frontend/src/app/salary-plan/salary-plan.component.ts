import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SalaryPlanService } from '../services/salary-plan.service';
import { AuthService } from '../services/auth.service';
import { CategoryService } from '../services/category.service';
import { Category } from '../models/category.model';
import { ExportService } from '../services/export.service';
import {
  SalaryPlan,
  SalaryPlanAllocation,
  SalaryPlanAllocationType,
  SalaryPlanCopyRequest,
  SalaryPlanIncomeSource,
  SalaryPlanRequest
} from '../models/salary-plan.model';

@Component({
  selector: 'app-salary-plan',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './salary-plan.component.html',
  styleUrls: ['./salary-plan.component.css']
})
export class SalaryPlanComponent implements OnInit {
  currentMonth: number;
  currentYear: number;
  salaryPlan: SalaryPlan | null = null;
  loading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  // Active Categories
  categories: Category[] = [];

  // Filter for allocations list
  allocationFilter: 'ALL' | SalaryPlanAllocationType = 'ALL';

  // Plan Edit / Create Modal
  showPlanModal: boolean = false;
  planForm: FormGroup;
  savingPlan: boolean = false;

  // Copy Plan Modal
  showCopyModal: boolean = false;
  copyForm: FormGroup;
  copyingPlan: boolean = false;

  // Delete Plan Modal
  showDeleteConfirmModal: boolean = false;
  deletingPlan: boolean = false;

  availableYears: number[] = [];

  monthNames: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  constructor(
    private fb: FormBuilder,
    private salaryPlanService: SalaryPlanService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private exportService: ExportService
  ) {
    const today = new Date();
    this.currentMonth = today.getMonth() + 1;
    this.currentYear = today.getFullYear();
    this.availableYears = this.generateYears(this.currentYear);

    this.planForm = this.fb.group({
      unallocatedCarryForward: [0, [Validators.min(0)]],
      incomeSources: this.fb.array([]),
      allocations: this.fb.array([]),
      trackEmergencyFund: [false],
      emergencyFundTarget: [0, [Validators.min(0)]],
      initialEmergencyFundBalance: [0, [Validators.min(0)]],
      notes: ['']
    });

    const prevMonth = this.currentMonth === 1 ? 12 : this.currentMonth - 1;
    const prevYear = this.currentMonth === 1 ? this.currentYear - 1 : this.currentYear;
    this.copyForm = this.fb.group({
      fromMonth: [prevMonth, Validators.required],
      fromYear: [prevYear, Validators.required],
      carryForwardUnallocated: [false]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadPlan();
  }

  loadCategories(): void {
    const userId = this.authService.getUserId();
    this.categoryService.getCategories(undefined, userId, false).subscribe({
      next: (cats) => {
        this.categories = cats || [];
      },
      error: () => {
        this.categories = [];
      }
    });
  }

  loadPlan(): void {
    const userId = this.authService.getUserId();
    this.loading = true;
    this.errorMessage = '';

    this.salaryPlanService.getSalaryPlan(userId, this.currentMonth, this.currentYear).subscribe({
      next: (plan) => {
        this.salaryPlan = plan;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load salary plan.';
      }
    });
  }

  generateYears(baseYear: number): number[] {
    const startYear = Math.min(2020, baseYear - 5);
    const endYear = Math.max(2035, baseYear + 10);
    const result: number[] = [];
    for (let y = startYear; y <= endYear; y++) {
      result.push(y);
    }
    return result;
  }

  previousMonth(): void {
    if (this.currentMonth === 1) {
      this.currentMonth = 12;
      this.currentYear -= 1;
      if (!this.availableYears.includes(this.currentYear)) {
        this.availableYears = this.generateYears(this.currentYear);
      }
    } else {
      this.currentMonth -= 1;
    }
    this.loadPlan();
  }

  nextMonth(): void {
    if (this.currentMonth === 12) {
      this.currentMonth = 1;
      this.currentYear += 1;
      if (!this.availableYears.includes(this.currentYear)) {
        this.availableYears = this.generateYears(this.currentYear);
      }
    } else {
      this.currentMonth += 1;
    }
    this.loadPlan();
  }

  onMonthChange(event: any): void {
    if (typeof event === 'number') {
      this.currentMonth = event;
    } else if (event?.target?.value) {
      this.currentMonth = Number(event.target.value);
    }
    this.loadPlan();
  }

  onYearChange(event: any): void {
    if (typeof event === 'number') {
      this.currentYear = event;
    } else if (event?.target?.value) {
      this.currentYear = Number(event.target.value);
    }
    this.loadPlan();
  }

  // --- FormArray Getters ---
  get incomeSourcesArray(): FormArray {
    return this.planForm.get('incomeSources') as FormArray;
  }

  get allocationsArray(): FormArray {
    return this.planForm.get('allocations') as FormArray;
  }

  // --- Modal Openers ---
  openCreatePlanModal(): void {
    this.initFormForPlan(null);
    this.showPlanModal = true;
  }

  openEditPlanModal(): void {
    this.initFormForPlan(this.salaryPlan);
    this.showPlanModal = true;
  }

  private initFormForPlan(plan: SalaryPlan | null): void {
    this.incomeSourcesArray.clear();
    this.allocationsArray.clear();

    if (plan && plan.isConfigured) {
      this.planForm.patchValue({
        unallocatedCarryForward: plan.unallocatedCarryForward || 0,
        trackEmergencyFund: (plan.emergencyFundTarget && plan.emergencyFundTarget > 0) || false,
        emergencyFundTarget: plan.emergencyFundTarget || 0,
        initialEmergencyFundBalance: plan.initialEmergencyFundBalance || 0,
        notes: plan.notes || ''
      });

      if (plan.incomeSources && plan.incomeSources.length > 0) {
        plan.incomeSources.forEach(s => this.addIncomeSource(s));
      } else if (plan.takeHomeSalary > 0) {
        this.addIncomeSource({ sourceName: 'Primary Salary', plannedAmount: plan.takeHomeSalary });
      } else {
        this.addIncomeSource({ sourceName: 'Primary Salary', plannedAmount: 0 });
      }

      if (plan.allocations && plan.allocations.length > 0) {
        plan.allocations.forEach(a => this.addAllocation(a));
      } else {
        // Fallback to legacy fields if allocations empty
        if (plan.plannedExpenses > 0) {
          this.addAllocation({ name: 'Living Expenses', plannedAmount: plan.plannedExpenses, allocationType: 'EXPENSE' });
        }
        if (plan.plannedEmergencyFund > 0) {
          this.addAllocation({ name: 'Emergency Fund', plannedAmount: plan.plannedEmergencyFund, allocationType: 'SAVINGS' });
        }
      }
    } else {
      // Clean slate for new plan
      this.planForm.patchValue({
        unallocatedCarryForward: 0,
        trackEmergencyFund: false,
        emergencyFundTarget: 0,
        initialEmergencyFundBalance: 0,
        notes: ''
      });
      this.addIncomeSource({ sourceName: 'Primary Income', plannedAmount: 0 });
    }
    this.errorMessage = '';
  }

  closePlanModal(): void {
    this.showPlanModal = false;
    this.savingPlan = false;
  }

  // --- Dynamic Income Sources & Allocations ---
  addIncomeSource(source?: Partial<SalaryPlanIncomeSource>): void {
    const group = this.fb.group({
      id: [source?.id || null],
      sourceName: [source?.sourceName || '', Validators.required],
      plannedAmount: [source?.plannedAmount ?? 0, [Validators.required, Validators.min(0)]]
    });
    this.incomeSourcesArray.push(group);
  }

  removeIncomeSource(index: number): void {
    this.incomeSourcesArray.removeAt(index);
  }

  addAllocation(alloc?: Partial<SalaryPlanAllocation>): void {
    const group = this.fb.group({
      id: [alloc?.id || null],
      name: [alloc?.name || '', Validators.required],
      plannedAmount: [alloc?.plannedAmount ?? 0, [Validators.required, Validators.min(0)]],
      allocationType: [alloc?.allocationType || 'EXPENSE', Validators.required],
      categoryId: [alloc?.categoryId || null]
    });
    this.allocationsArray.push(group);
  }

  removeAllocation(index: number): void {
    this.allocationsArray.removeAt(index);
  }

  onCategorySelected(allocIndex: number, event: Event): void {
    const select = event.target as HTMLSelectElement;
    const catId = Number(select.value);
    const cat = this.categories.find(c => c.id === catId);
    if (cat) {
      const row = this.allocationsArray.at(allocIndex);
      if (!row.get('name')?.value || row.get('name')?.value.trim() === '') {
        row.patchValue({ name: cat.name });
      }
      if (cat.type === 'SAVINGS') {
        row.patchValue({ allocationType: 'SAVINGS' });
      } else if (cat.type === 'EXPENSE') {
        row.patchValue({ allocationType: 'EXPENSE' });
      } else if (cat.type === 'OTHER') {
        row.patchValue({ allocationType: 'OTHER' });
      }
    }
  }

  populateFromCategories(): void {
    if (!this.categories || this.categories.length === 0) return;
    this.categories.forEach(cat => {
      let type: SalaryPlanAllocationType = 'EXPENSE';
      if (cat.type === 'SAVINGS') type = 'SAVINGS';
      else if (cat.type === 'OTHER') type = 'OTHER';

      // Check if already in allocations
      const exists = this.allocationsArray.controls.some(ctrl => ctrl.get('categoryId')?.value === cat.id);
      if (!exists) {
        this.addAllocation({
          name: cat.name,
          categoryId: cat.id,
          allocationType: type,
          plannedAmount: 0
        });
      }
    });
  }

  // --- Real-Time Calculated Values in Modal ---
  getLiveTotalIncome(): number {
    const carryForward = Number(this.planForm.get('unallocatedCarryForward')?.value || 0);
    let sourcesTotal = 0;
    for (const ctrl of this.incomeSourcesArray.controls) {
      sourcesTotal += Number(ctrl.get('plannedAmount')?.value || 0);
    }
    return sourcesTotal + carryForward;
  }

  getLiveTotalAllocations(): number {
    let total = 0;
    for (const ctrl of this.allocationsArray.controls) {
      total += Number(ctrl.get('plannedAmount')?.value || 0);
    }
    return total;
  }

  getLiveAllocationsByType(type: SalaryPlanAllocationType): number {
    let sum = 0;
    for (const ctrl of this.allocationsArray.controls) {
      if (ctrl.get('allocationType')?.value === type) {
        sum += Number(ctrl.get('plannedAmount')?.value || 0);
      }
    }
    return sum;
  }

  getLiveBuffer(): number {
    return this.getLiveTotalIncome() - this.getLiveTotalAllocations();
  }

  // --- Save Plan ---
  savePlan(): void {
    if (this.planForm.invalid) {
      this.planForm.markAllAsTouched();
      return;
    }

    const userId = this.authService.getUserId();
    this.savingPlan = true;
    this.errorMessage = '';

    const formVal = this.planForm.value;

    const incomeSources: SalaryPlanIncomeSource[] = this.incomeSourcesArray.controls.map((ctrl, index) => ({
      id: ctrl.get('id')?.value || undefined,
      sourceName: ctrl.get('sourceName')?.value?.trim() || `Income ${index + 1}`,
      plannedAmount: Number(ctrl.get('plannedAmount')?.value || 0),
      orderIndex: index
    }));

    const allocations: SalaryPlanAllocation[] = this.allocationsArray.controls.map((ctrl, index) => ({
      id: ctrl.get('id')?.value || undefined,
      name: ctrl.get('name')?.value?.trim() || `Allocation ${index + 1}`,
      plannedAmount: Number(ctrl.get('plannedAmount')?.value || 0),
      allocationType: ctrl.get('allocationType')?.value as SalaryPlanAllocationType,
      categoryId: ctrl.get('categoryId')?.value ? Number(ctrl.get('categoryId')?.value) : undefined,
      orderIndex: index
    }));

    const req: SalaryPlanRequest = {
      month: this.currentMonth,
      year: this.currentYear,
      incomeSources: incomeSources,
      allocations: allocations,
      unallocatedCarryForward: Number(formVal.unallocatedCarryForward || 0),
      emergencyFundTarget: formVal.trackEmergencyFund ? Number(formVal.emergencyFundTarget || 0) : 0,
      initialEmergencyFundBalance: formVal.trackEmergencyFund ? Number(formVal.initialEmergencyFundBalance || 0) : 0,
      notes: formVal.notes ? formVal.notes.trim() : undefined,
      userId: userId
    };

    this.salaryPlanService.saveSalaryPlan(req).subscribe({
      next: (updatedPlan) => {
        this.salaryPlan = updatedPlan;
        this.savingPlan = false;
        this.closePlanModal();
        this.successMessage = `Budget plan for ${this.monthNames[this.currentMonth - 1]} ${this.currentYear} saved successfully!`;
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.savingPlan = false;
        this.errorMessage = err.error?.message || 'Failed to save salary plan.';
      }
    });
  }

  // --- Copy Plan ---
  openCopyModal(): void {
    const prevMonth = this.currentMonth === 1 ? 12 : this.currentMonth - 1;
    const prevYear = this.currentMonth === 1 ? this.currentYear - 1 : this.currentYear;
    this.copyForm.patchValue({
      fromMonth: prevMonth,
      fromYear: prevYear,
      carryForwardUnallocated: false
    });
    this.showCopyModal = true;
  }

  closeCopyModal(): void {
    this.showCopyModal = false;
    this.copyingPlan = false;
  }

  executeCopyPlan(): void {
    if (this.copyForm.invalid) return;

    const userId = this.authService.getUserId();
    this.copyingPlan = true;
    this.errorMessage = '';

    const req: SalaryPlanCopyRequest = {
      fromMonth: Number(this.copyForm.value.fromMonth),
      fromYear: Number(this.copyForm.value.fromYear),
      toMonth: this.currentMonth,
      toYear: this.currentYear,
      carryForwardUnallocated: Boolean(this.copyForm.value.carryForwardUnallocated),
      userId: userId
    };

    this.salaryPlanService.copySalaryPlan(req).subscribe({
      next: (copiedPlan) => {
        this.salaryPlan = copiedPlan;
        this.copyingPlan = false;
        this.closeCopyModal();
        this.successMessage = `Plan copied successfully from ${this.monthNames[req.fromMonth - 1]} ${req.fromYear} to ${this.monthNames[this.currentMonth - 1]} ${this.currentYear}!`;
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.copyingPlan = false;
        this.errorMessage = err.error?.message || 'Failed to copy salary plan.';
      }
    });
  }

  // --- Delete Plan ---
  openDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = true;
  }

  closeDeleteConfirmModal(): void {
    this.showDeleteConfirmModal = false;
    this.deletingPlan = false;
  }

  executeDeletePlan(): void {
    const userId = this.authService.getUserId();
    this.deletingPlan = true;
    this.errorMessage = '';

    this.salaryPlanService.deleteSalaryPlanByMonthYear(userId, this.currentMonth, this.currentYear).subscribe({
      next: () => {
        this.deletingPlan = false;
        this.closeDeleteConfirmModal();
        this.successMessage = `Budget plan for ${this.monthNames[this.currentMonth - 1]} ${this.currentYear} deleted. Your transaction history is intact.`;
        this.loadPlan();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.deletingPlan = false;
        this.errorMessage = err.error?.message || 'Failed to delete budget plan.';
      }
    });
  }

  // --- Helpers for Display ---
  getCategoryName(categoryId?: number): string {
    if (!categoryId) return '';
    const cat = this.categories.find(c => c.id === categoryId);
    return cat ? cat.name : '';
  }

  getAllocationsFiltered(): SalaryPlanAllocation[] {
    if (!this.salaryPlan || !this.salaryPlan.allocations) return [];
    if (this.allocationFilter === 'ALL') {
      return this.salaryPlan.allocations;
    }
    return this.salaryPlan.allocations.filter(a => a.allocationType === this.allocationFilter);
  }

  getAllocationTypeBadgeClass(type: SalaryPlanAllocationType): string {
    switch (type) {
      case 'EXPENSE': return 'bg-danger-subtle text-danger border-danger-subtle';
      case 'SAVINGS': return 'bg-success-subtle text-success border-success-subtle';
      case 'OTHER': return 'bg-secondary-subtle text-secondary border-secondary-subtle';
      default: return 'bg-light text-dark';
    }
  }

  getPercentageOfIncome(amount: number): number {
    if (!this.salaryPlan || this.salaryPlan.totalPlannedIncome <= 0) return 0;
    return Math.round((amount / this.salaryPlan.totalPlannedIncome) * 100);
  }

  exportCsv(): void {
    const userId = this.authService.getUserId();
    this.exportService.downloadSalaryPlanCsv(userId, this.currentMonth, this.currentYear);
  }

  printStatement(): void {
    this.exportService.printCurrentPage();
  }
}
