import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { SalaryPlan, SalaryPlanCopyRequest, SalaryPlanRequest } from '../models/salary-plan.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SalaryPlanService {
  private apiUrl = `${environment.apiBaseUrl}/api/salary-plan`;

  constructor(private http: HttpClient) {}

  private normalize(plan: SalaryPlan): SalaryPlan {
    if (plan) {
      plan.isConfigured = plan.isConfigured ?? (plan as any).configured ?? false;
      plan.isOverAllocated = plan.isOverAllocated ?? (plan as any).overAllocated ?? false;
      plan.incomeSources = plan.incomeSources || [];
      plan.allocations = plan.allocations || [];
    }
    return plan;
  }

  getSalaryPlan(userId: number, month?: number, year?: number): Observable<SalaryPlan> {
    const safeUserId = (userId && !isNaN(Number(userId))) ? Number(userId) : 1;
    let params = new HttpParams();
    if (month) params = params.set('month', month.toString());
    if (year) params = params.set('year', year.toString());
    return this.http.get<SalaryPlan>(`${this.apiUrl}/user/${safeUserId}`, { params }).pipe(
      map(p => this.normalize(p))
    );
  }

  saveSalaryPlan(request: SalaryPlanRequest): Observable<SalaryPlan> {
    if (!request.userId || isNaN(Number(request.userId))) {
      request.userId = 1;
    }
    return this.http.post<SalaryPlan>(this.apiUrl, request).pipe(
      map(p => this.normalize(p))
    );
  }

  copySalaryPlan(request: SalaryPlanCopyRequest): Observable<SalaryPlan> {
    if (!request.userId || isNaN(Number(request.userId))) {
      request.userId = 1;
    }
    return this.http.post<SalaryPlan>(`${this.apiUrl}/copy`, request).pipe(
      map(p => this.normalize(p))
    );
  }

  deleteSalaryPlan(id: number, userId?: number): Observable<void> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { params });
  }

  deleteSalaryPlanByMonthYear(userId: number, month: number, year: number): Observable<void> {
    let params = new HttpParams()
      .set('month', month.toString())
      .set('year', year.toString());
    return this.http.delete<void>(`${this.apiUrl}/user/${userId}`, { params });
  }
}
