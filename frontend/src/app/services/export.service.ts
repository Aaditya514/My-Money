import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DialogService } from './dialog.service';

@Injectable({
  providedIn: 'root'
})
export class ExportService {
  private apiUrl = `${environment.apiBaseUrl}/api/export`;

  constructor(
    private http: HttpClient,
    private dialogService: DialogService
  ) {}

  downloadTransactionsCsv(userId: number, fromDate?: string, toDate?: string): void {
    let url = `${this.apiUrl}/transactions/csv?userId=${userId}`;
    if (fromDate) url += `&fromDate=${fromDate}`;
    if (toDate) url += `&toDate=${toDate}`;

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `transactions_${new Date().toISOString().slice(0, 10)}.csv`;
        link.click();
        window.URL.revokeObjectURL(downloadUrl);
      },
      error: (err) => {
        console.error('Failed to export transactions CSV', err);
        this.dialogService.alert({
          title: 'Export Failed',
          message: 'Failed to download transactions CSV export. Please verify server connection and try again.',
          type: 'danger'
        });
      }
    });
  }

  downloadSalaryPlanCsv(userId: number, month: number, year: number): void {
    const url = `${this.apiUrl}/salary-plan/csv?userId=${userId}&month=${month}&year=${year}`;

    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = `salary_plan_${year}_${String(month).padStart(2, '0')}.csv`;
        link.click();
        window.URL.revokeObjectURL(downloadUrl);
      },
      error: (err) => {
        console.error('Failed to export salary plan CSV', err);
        this.dialogService.alert({
          title: 'Export Failed',
          message: 'Failed to download salary plan CSV export. Please try again.',
          type: 'danger'
        });
      }
    });
  }

  printCurrentPage(): void {
    window.print();
  }
}
