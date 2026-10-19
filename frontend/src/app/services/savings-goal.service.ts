import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SavingsGoal, SavingsGoalRequest, GoalContribution, GoalContributionRequest } from '../models/savings-goal.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SavingsGoalService {
  private apiUrl = `${environment.apiBaseUrl}/api/savings-goals`;

  constructor(private http: HttpClient) {}

  getGoals(userId: number): Observable<SavingsGoal[]> {
    const safeUserId = (userId && !isNaN(Number(userId))) ? Number(userId) : 1;
    return this.http.get<SavingsGoal[]>(`${this.apiUrl}/user/${safeUserId}`);
  }

  getGoalById(id: number, userId?: number): Observable<SavingsGoal> {
    const safeUserId = (userId && !isNaN(Number(userId))) ? Number(userId) : 1;
    let params = new HttpParams().set('userId', safeUserId.toString());
    return this.http.get<SavingsGoal>(`${this.apiUrl}/${id}`, { params });
  }

  createGoal(request: SavingsGoalRequest): Observable<SavingsGoal> {
    if (!request.userId || isNaN(Number(request.userId))) {
      request.userId = 1;
    }
    return this.http.post<SavingsGoal>(this.apiUrl, request);
  }

  updateGoal(id: number, request: SavingsGoalRequest, userId?: number): Observable<SavingsGoal> {
    const safeUserId = (userId && !isNaN(Number(userId))) ? Number(userId) : 1;
    let params = new HttpParams().set('userId', safeUserId.toString());
    return this.http.put<SavingsGoal>(`${this.apiUrl}/${id}`, request, { params });
  }

  deleteGoal(id: number, userId?: number): Observable<void> {
    const safeUserId = (userId && !isNaN(Number(userId))) ? Number(userId) : 1;
    let params = new HttpParams().set('userId', safeUserId.toString());
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { params });
  }

  contributeToGoal(goalId: number, request: GoalContributionRequest, userId?: number): Observable<GoalContribution> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<GoalContribution>(`${this.apiUrl}/${goalId}/contribute`, request, { params });
  }

  getContributions(goalId: number, userId?: number): Observable<GoalContribution[]> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<GoalContribution[]>(`${this.apiUrl}/${goalId}/contributions`, { params });
  }

  toggleComplete(goalId: number, userId?: number): Observable<SavingsGoal> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.patch<SavingsGoal>(`${this.apiUrl}/${goalId}/toggle-complete`, {}, { params });
  }
}
