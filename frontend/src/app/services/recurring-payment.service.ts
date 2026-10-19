import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecurringPayment, RecurringPaymentRequest, RecurringPaymentRecordResult } from '../models/recurring-payment.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RecurringPaymentService {
  private apiUrl = `${environment.apiBaseUrl}/api/recurring-payments`;

  constructor(private http: HttpClient) {}

  getRecurringPaymentsByUserId(userId: number): Observable<RecurringPayment[]> {
    return this.http.get<RecurringPayment[]>(`${this.apiUrl}/user/${userId}`);
  }

  getUpcomingPayments(userId: number, days: number = 14): Observable<RecurringPayment[]> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<RecurringPayment[]>(`${this.apiUrl}/user/${userId}/upcoming`, { params });
  }

  getRecurringPaymentById(id: number, userId?: number): Observable<RecurringPayment> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.get<RecurringPayment>(`${this.apiUrl}/${id}`, { params });
  }

  createRecurringPayment(request: RecurringPaymentRequest): Observable<RecurringPayment> {
    return this.http.post<RecurringPayment>(this.apiUrl, request);
  }

  updateRecurringPayment(id: number, request: RecurringPaymentRequest, userId?: number): Observable<RecurringPayment> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.put<RecurringPayment>(`${this.apiUrl}/${id}`, request, { params });
  }

  deleteRecurringPayment(id: number, userId?: number): Observable<void> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { params });
  }

  toggleActive(id: number, userId?: number): Observable<RecurringPayment> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.patch<RecurringPayment>(`${this.apiUrl}/${id}/toggle`, {}, { params });
  }

  recordPayment(id: number, userId?: number): Observable<RecurringPaymentRecordResult> {
    let params = new HttpParams();
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    return this.http.post<RecurringPaymentRecordResult>(`${this.apiUrl}/${id}/record`, {}, { params });
  }
}
