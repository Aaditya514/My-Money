import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { DashboardService } from '../services/dashboard.service';
import { AuthService } from '../services/auth.service';
import { RecurringPaymentService } from '../services/recurring-payment.service';
import { DashboardData } from '../models/dashboard.model';
import { RecurringPayment } from '../models/recurring-payment.model';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild('categoryDoughnutCanvas') categoryCanvasRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('cashFlowBarCanvas') cashFlowCanvasRef?: ElementRef<HTMLCanvasElement>;

  dashboardData: DashboardData | null = null;
  upcomingBills: RecurringPayment[] = [];
  recordingBillId: number | null = null;
  loading: boolean = true;
  errorMessage: string = '';
  successMessage: string = '';

  private categoryChart: Chart | null = null;
  private cashFlowChart: Chart | null = null;

  // Segmented Switcher (Overview vs Activity)
  activeSegment: 'ov' | 'act' = 'ov';

  // Month & Year Filter
  selectedMonth: number;
  selectedYear: number;

  readonly months: string[] = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  years: number[];

  private readonly chartColors: string[] = [
    '#6366f1', '#ec4899', '#10b981', '#f59e0b', '#8b5cf6',
    '#06b6d4', '#f97316', '#14b8a6', '#ef4444', '#3b82f6', '#84cc16'
  ];

  constructor(
    private dashboardService: DashboardService,
    private recurringService: RecurringPaymentService,
    private authService: AuthService
  ) {
    const today = new Date();
    this.selectedMonth = today.getMonth() + 1;
    this.selectedYear = today.getFullYear();
    this.years = this.generateYears(this.selectedYear);
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

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  loadDashboardData(): void {
    const userId = this.authService.getUserId();
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getDashboardData(userId, this.selectedMonth, this.selectedYear).subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.loading = false;
        setTimeout(() => this.renderCharts(), 50);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load dashboard data. Please try again later.';
      }
    });

    this.loadUpcomingBills();
  }

  loadUpcomingBills(): void {
    const userId = this.authService.getUserId();
    this.recurringService.getUpcomingPayments(userId, 30).subscribe({
      next: (bills) => {
        this.upcomingBills = bills;
      },
      error: () => {}
    });
  }

  onRecordBill(bill: RecurringPayment): void {
    if (this.recordingBillId) return;
    this.recordingBillId = bill.id;
    const userId = this.authService.getUserId();

    this.recurringService.recordPayment(bill.id, userId).subscribe({
      next: (result) => {
        this.recordingBillId = null;
        this.successMessage = result.message || `Recorded ₹${bill.amount} payment for '${bill.title}'!`;
        this.loadDashboardData();
        setTimeout(() => {
          this.successMessage = '';
        }, 4000);
      },
      error: (err) => {
        this.recordingBillId = null;
        this.errorMessage = err.error?.message || 'Failed to record bill payment.';
      }
    });
  }

  private destroyCharts(): void {
    if (this.categoryChart) {
      this.categoryChart.destroy();
      this.categoryChart = null;
    }
    if (this.cashFlowChart) {
      this.cashFlowChart.destroy();
      this.cashFlowChart = null;
    }
  }

  private renderCharts(): void {
    if (!this.dashboardData) return;
    this.destroyCharts();

    this.renderCategoryChart();
    this.renderCashFlowChart();
  }

  private renderCategoryChart(): void {
    if (!this.categoryCanvasRef?.nativeElement || !this.dashboardData) return;

    const ctx = this.categoryCanvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const catExpenses = this.dashboardData.categoryExpenses || [];
    if (catExpenses.length === 0) return;

    const labels = catExpenses.map((c) => c.categoryName);
    const amounts = catExpenses.map((c) => c.amount);
    const colors = labels.map((_, i) => this.chartColors[i % this.chartColors.length]);

    this.categoryChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: amounts,
            backgroundColor: colors,
            borderWidth: 2,
            borderColor: '#ffffff',
            hoverOffset: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '65%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 12,
              padding: 12,
              font: {
                size: 11
              }
            }
          },
          tooltip: {
            callbacks: {
              label: (item) => {
                const val = Number(item.raw || 0);
                const total = amounts.reduce((a, b) => a + b, 0);
                const pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0';
                return ` ${item.label}: ₹${val.toLocaleString('en-IN', { minimumFractionDigits: 2 })} (${pct}%)`;
              }
            }
          }
        }
      }
    });
  }

  private renderCashFlowChart(): void {
    if (!this.cashFlowCanvasRef?.nativeElement || !this.dashboardData) return;

    const ctx = this.cashFlowCanvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const totalIncome = this.dashboardData.totalIncome || (this.dashboardData.salaryPlan ? this.dashboardData.salaryPlan.takeHomeSalary : 0);
    const totalExpenses = this.dashboardData.totalExpenses || (this.dashboardData.salaryPlan ? this.dashboardData.salaryPlan.actualExpenses : 0);
    const buffer = this.dashboardData.salaryPlan ? this.dashboardData.salaryPlan.calculatedBuffer : (totalIncome - totalExpenses);

    this.cashFlowChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: ['Total Inflow', 'Total Outflow', 'Net Buffer / Surplus'],
        datasets: [
          {
            label: 'Amount (₹)',
            data: [totalIncome, totalExpenses, Math.max(0, buffer)],
            backgroundColor: ['#10b981', '#ef4444', '#3b82f6'],
            borderRadius: 6,
            maxBarThickness: 45
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: (item) => ` ₹${Number(item.raw || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: (val) => '₹' + Number(val).toLocaleString('en-IN')
            },
            grid: {
              color: '#f1f5f9'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  prevMonth(): void {
    if (this.selectedMonth === 1) {
      this.selectedMonth = 12;
      this.selectedYear--;
      if (!this.years.includes(this.selectedYear)) {
        this.years = this.generateYears(this.selectedYear);
      }
    } else {
      this.selectedMonth--;
    }
    this.loadDashboardData();
  }

  nextMonth(): void {
    if (this.selectedMonth === 12) {
      this.selectedMonth = 1;
      this.selectedYear++;
      if (!this.years.includes(this.selectedYear)) {
        this.years = this.generateYears(this.selectedYear);
      }
    } else {
      this.selectedMonth++;
    }
    this.loadDashboardData();
  }

  onMonthYearChange(): void {
    this.loadDashboardData();
  }

  getUserName(): string {
    return this.authService.getCurrentUser()?.name || 'User';
  }

  getSalaryExpensePercentage(): number {
    if (!this.dashboardData?.salaryPlan || this.dashboardData.salaryPlan.takeHomeSalary <= 0) return 0;
    return Math.min(
      100,
      Math.round((this.dashboardData.salaryPlan.actualExpenses / this.dashboardData.salaryPlan.takeHomeSalary) * 100)
    );
  }

  getSalaryPlannedExpensePercentage(): number {
    if (!this.dashboardData?.salaryPlan || this.dashboardData.salaryPlan.takeHomeSalary <= 0) return 0;
    return Math.min(
      100,
      Math.round((this.dashboardData.salaryPlan.plannedExpenses / this.dashboardData.salaryPlan.takeHomeSalary) * 100)
    );
  }

  getInflowRatio(): number {
    const inc = this.dashboardData?.totalIncome || 0;
    const exp = this.dashboardData?.totalExpenses || 0;
    const total = inc + exp;
    if (total <= 0) return 50;
    return Math.min(100, Math.max(5, Math.round((inc / total) * 100)));
  }

  getOutflowRatio(): number {
    return 100 - this.getInflowRatio();
  }

  getIntPart(val: number | undefined): string {
    const n = Math.abs(val || 0);
    const intStr = Math.floor(n).toLocaleString('en-IN');
    return intStr;
  }

  getDecPart(val: number | undefined): string {
    const n = Math.abs(val || 0);
    const dec = (n % 1).toFixed(2).slice(1); // e.g. ".00"
    return dec;
  }

  getCategoryIcon(catName: string | undefined): string {
    const name = (catName || '').toLowerCase();
    if (name.includes('food') || name.includes('dining') || name.includes('restaurant')) return 'utensils';
    if (name.includes('groc') || name.includes('supermarket') || name.includes('market')) return 'cart';
    if (name.includes('transport') || name.includes('fuel') || name.includes('cab') || name.includes('auto') || name.includes('travel')) return 'car';
    if (name.includes('shop') || name.includes('cloth') || name.includes('mall') || name.includes('amazon')) return 'bag';
    if (name.includes('util') || name.includes('bill') || name.includes('electric') || name.includes('recharge') || name.includes('wifi')) return 'zap';
    if (name.includes('movie') || name.includes('entertain') || name.includes('netflix') || name.includes('game')) return 'film';
    if (name.includes('health') || name.includes('med') || name.includes('doctor') || name.includes('fitness')) return 'heart';
    if (name.includes('salary') || name.includes('income') || name.includes('job') || name.includes('bonus') || name.includes('freelance')) return 'brief';
    if (name.includes('invest') || name.includes('stock') || name.includes('mutual') || name.includes('goal')) return 'target';
    if (name.includes('subscript') || name.includes('recur') || name.includes('emi') || name.includes('rent')) return 'repeat';
    return 'layers';
  }
}
