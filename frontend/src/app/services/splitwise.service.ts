import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  SplitBalanceSummary,
  SplitExpenseRequest,
  SplitExpenseResponse,
  SplitGroup,
  SplitGroupRequest,
  SplitMember,
  SplitMemberRequest,
  SplitSettlementRequest,
  SplitSettlementResponse,
  SplitContact
} from '../models/splitwise.model';

@Injectable({
  providedIn: 'root'
})
export class SplitwiseService {
  private apiUrl = `${environment.apiBaseUrl}/api/splitwise`;

  constructor(private http: HttpClient) {}

  // ==========================================
  // Groups
  // ==========================================

  getGroups(userId: number): Observable<SplitGroup[]> {
    return this.http.get<SplitGroup[]>(`${this.apiUrl}/groups/user/${userId}`);
  }

  getGroupById(groupId: number, userId?: number): Observable<SplitGroup> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<SplitGroup>(`${this.apiUrl}/groups/${groupId}`, { params });
  }

  createGroup(request: SplitGroupRequest, userId?: number): Observable<SplitGroup> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<SplitGroup>(`${this.apiUrl}/groups`, request, { params });
  }

  updateGroup(groupId: number, request: SplitGroupRequest, userId?: number): Observable<SplitGroup> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.put<SplitGroup>(`${this.apiUrl}/groups/${groupId}`, request, { params });
  }

  deleteGroup(groupId: number, userId?: number): Observable<void> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.delete<void>(`${this.apiUrl}/groups/${groupId}`, { params });
  }

  // ==========================================
  // Members
  // ==========================================

  getMembers(groupId: number, userId?: number): Observable<SplitMember[]> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<SplitMember[]>(`${this.apiUrl}/groups/${groupId}/members`, { params });
  }

  addMember(groupId: number, request: SplitMemberRequest, userId?: number): Observable<SplitMember> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<SplitMember>(`${this.apiUrl}/groups/${groupId}/members`, request, { params });
  }

  removeMember(groupId: number, memberId: number, userId?: number): Observable<void> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.delete<void>(`${this.apiUrl}/groups/${groupId}/members/${memberId}`, { params });
  }

  // ==========================================
  // Expenses
  // ==========================================

  getExpenses(groupId: number, userId?: number): Observable<SplitExpenseResponse[]> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<SplitExpenseResponse[]>(`${this.apiUrl}/groups/${groupId}/expenses`, { params });
  }

  createExpense(groupId: number, request: SplitExpenseRequest, userId?: number): Observable<SplitExpenseResponse> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<SplitExpenseResponse>(`${this.apiUrl}/groups/${groupId}/expenses`, request, { params });
  }

  updateExpense(groupId: number, expenseId: number, request: SplitExpenseRequest, userId?: number): Observable<SplitExpenseResponse> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.put<SplitExpenseResponse>(`${this.apiUrl}/groups/${groupId}/expenses/${expenseId}`, request, { params });
  }

  deleteExpense(groupId: number, expenseId: number, userId?: number): Observable<void> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.delete<void>(`${this.apiUrl}/groups/${groupId}/expenses/${expenseId}`, { params });
  }

  // ==========================================
  // 1-Tap Ledger Synchronization
  // ==========================================

  syncExpenseToLedger(groupId: number, expenseId: number, userId?: number): Observable<SplitExpenseResponse> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<SplitExpenseResponse>(`${this.apiUrl}/groups/${groupId}/expenses/${expenseId}/sync-to-ledger`, {}, { params });
  }

  // ==========================================
  // Settlements
  // ==========================================

  getSettlements(groupId: number, userId?: number): Observable<SplitSettlementResponse[]> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<SplitSettlementResponse[]>(`${this.apiUrl}/groups/${groupId}/settlements`, { params });
  }

  createSettlement(groupId: number, request: SplitSettlementRequest, userId?: number): Observable<SplitSettlementResponse> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.post<SplitSettlementResponse>(`${this.apiUrl}/groups/${groupId}/settlements`, request, { params });
  }

  // ==========================================
  // Balances & Debt Simplification
  // ==========================================

  getBalanceSummary(groupId: number, userId?: number): Observable<SplitBalanceSummary> {
    let params = new HttpParams();
    if (userId) params = params.set('userId', userId.toString());
    return this.http.get<SplitBalanceSummary>(`${this.apiUrl}/groups/${groupId}/balances`, { params });
  }

  // ==========================================
  // Contacts, Search & Client Cache
  // ==========================================

  getContacts(userId: number): Observable<SplitContact[]> {
    let params = new HttpParams().set('userId', userId.toString());
    return this.http.get<SplitContact[]>(`${this.apiUrl}/contacts`, { params });
  }

  searchUsers(query: string): Observable<SplitContact[]> {
    let params = new HttpParams().set('query', query);
    return this.http.get<SplitContact[]>(`${this.apiUrl}/search-users`, { params });
  }

  private getCacheKey(userId: number): string {
    return `splitwise_contacts_cache_${userId}`;
  }

  private getNicknamesKey(userId: number): string {
    return `splitwise_nicknames_${userId}`;
  }

  getCachedContacts(userId: number): SplitContact[] {
    try {
      const raw = localStorage.getItem(this.getCacheKey(userId));
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  setCachedContacts(userId: number, contacts: SplitContact[]): void {
    try {
      localStorage.setItem(this.getCacheKey(userId), JSON.stringify(contacts));
    } catch (e) {
      console.error('Failed to cache contacts', e);
    }
  }

  getUserNicknames(userId: number): Record<string, string> {
    try {
      const raw = localStorage.getItem(this.getNicknamesKey(userId));
      return raw ? JSON.parse(raw) : {};
    } catch {
      return {};
    }
  }

  setUserNickname(userId: number, contactKey: string, nickname: string): void {
    try {
      const nicknames = this.getUserNicknames(userId);
      const cleanKey = contactKey.trim().toLowerCase();
      if (nickname && nickname.trim()) {
        nicknames[cleanKey] = nickname.trim();
      } else {
        delete nicknames[cleanKey];
      }
      localStorage.setItem(this.getNicknamesKey(userId), JSON.stringify(nicknames));
    } catch (e) {
      console.error('Failed to save nickname', e);
    }
  }
}
