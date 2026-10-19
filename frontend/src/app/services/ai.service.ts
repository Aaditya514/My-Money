import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AiBillScanResponse,
  AiGroupBillScanRequest,
  AiGroupBillScanResponse,
  AiTextScanRequest,
  GroupMemberContext
} from '../models/ai-scan.model';

import { environment } from '../../environments/environment';

import { HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = `${environment.apiBaseUrl}/api/ai`;
  private readonly API_KEY_STORAGE = 'finance_custom_gemini_key';

  constructor(private http: HttpClient) {}

  /**
   * Get custom Gemini API key from localStorage
   */
  getStoredApiKey(): string | null {
    try {
      return localStorage.getItem(this.API_KEY_STORAGE);
    } catch {
      return null;
    }
  }

  /**
   * Save custom Gemini API key into localStorage
   */
  setStoredApiKey(key: string): void {
    if (key && key.trim()) {
      localStorage.setItem(this.API_KEY_STORAGE, key.trim());
    } else {
      this.clearStoredApiKey();
    }
  }

  /**
   * Remove custom Gemini API key
   */
  clearStoredApiKey(): void {
    localStorage.removeItem(this.API_KEY_STORAGE);
  }

  private getAuthHeaders(customKey?: string): HttpHeaders {
    let headers = new HttpHeaders();
    const key = customKey || this.getStoredApiKey();
    if (key && key.trim()) {
      headers = headers.set('X-Gemini-Api-Key', key.trim());
    }
    return headers;
  }

  /**
   * Send one or multiple receipt/bill images to Gemini AI
   */
  scanReceiptImages(files: File[], customApiKey?: string): Observable<AiBillScanResponse> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file);
    }
    const headers = this.getAuthHeaders(customApiKey);
    return this.http.post<AiBillScanResponse>(`${this.apiUrl}/scan-receipt`, formData, { headers });
  }

  /**
   * Backward-compatible single file upload
   */
  scanReceiptImage(file: File, customApiKey?: string): Observable<AiBillScanResponse> {
    return this.scanReceiptImages([file], customApiKey);
  }

  /**
   * Send a text bill statement or SMS notification to Gemini AI
   */
  scanBillStatement(statement: string, customApiKey?: string): Observable<AiBillScanResponse> {
    const request: AiTextScanRequest = { statement };
    const headers = this.getAuthHeaders(customApiKey);
    return this.http.post<AiBillScanResponse>(`${this.apiUrl}/scan-statement`, request, { headers });
  }

  /**
   * Scan group receipt image(s) and split items among group members with custom order notes
   */
  scanGroupReceiptImages(
    files: File[],
    groupMembers: GroupMemberContext[],
    orderNotes?: string,
    defaultPayerMemberId?: number,
    customApiKey?: string
  ): Observable<AiGroupBillScanResponse> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file);
    }
    formData.append('groupMembers', JSON.stringify(groupMembers));
    if (orderNotes && orderNotes.trim()) {
      formData.append('orderNotes', orderNotes.trim());
    }
    if (defaultPayerMemberId) {
      formData.append('defaultPayerMemberId', defaultPayerMemberId.toString());
    }

    const headers = this.getAuthHeaders(customApiKey);
    return this.http.post<AiGroupBillScanResponse>(`${this.apiUrl}/split-group-bill`, formData, { headers });
  }

  /**
   * Scan text bill statement and split items among group members with custom order notes
   */
  scanGroupBillStatement(
    statement: string,
    groupMembers: GroupMemberContext[],
    orderNotes?: string,
    defaultPayerMemberId?: number,
    customApiKey?: string
  ): Observable<AiGroupBillScanResponse> {
    const request: AiGroupBillScanRequest = {
      statement,
      groupMembers,
      orderNotes,
      defaultPayerMemberId
    };
    const headers = this.getAuthHeaders(customApiKey);
    return this.http.post<AiGroupBillScanResponse>(`${this.apiUrl}/split-group-statement`, request, { headers });
  }

  /**
   * Get server/configured API key status
   */
  getKeyStatus(): Observable<{ configured: boolean; maskedKey: string }> {
    return this.http.get<{ configured: boolean; maskedKey: string }>(`${this.apiUrl}/key/status`);
  }

  /**
   * Test an API key with a ping prompt to Gemini
   */
  testApiKey(apiKey?: string): Observable<{ valid: boolean; message?: string; model?: string }> {
    const keyToTest = apiKey || this.getStoredApiKey() || '';
    return this.http.post<{ valid: boolean; message?: string; model?: string }>(
      `${this.apiUrl}/key/test`,
      { apiKey: keyToTest }
    );
  }

  /**
   * Update the in-memory key on the server
   */
  updateServerKey(apiKey: string): Observable<{ success: boolean; maskedKey: string }> {
    return this.http.post<{ success: boolean; maskedKey: string }>(
      `${this.apiUrl}/key/update`,
      { apiKey }
    );
  }
}
