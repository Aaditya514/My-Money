import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, SendOtpResponse, User } from '../models/user.model';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiBaseUrl}/api/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  private getStoredUser(): User | null {
    const token = localStorage.getItem('authToken');
    const userJson = localStorage.getItem('currentUser');
    if (!token || !userJson) {
      // Clean up stale or partial session
      localStorage.removeItem('currentUser');
      localStorage.removeItem('authToken');
      return null;
    }

    try {
      const u = JSON.parse(userJson);
      if (!u) return null;
      // Unpack nested response if stored as { user: { ... } }
      const raw = u.user ? u.user : u;
      let id = raw.id !== undefined && raw.id !== null ? Number(raw.id) : null;
      if (!id || isNaN(id)) {
        id = 1;
      }

      const normalized: User = {
        id: id,
        name: raw.name || 'User',
        email: raw.email || '',
        phoneNumber: raw.phoneNumber || ''
      };

      if (raw.id !== id) {
        localStorage.setItem('currentUser', JSON.stringify(normalized));
      }

      return normalized;
    } catch (e) {
      localStorage.removeItem('currentUser');
      localStorage.removeItem('authToken');
      return null;
    }
  }

  public getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  public getUserId(): number {
    const u = this.getCurrentUser();
    if (u && u.id && !isNaN(Number(u.id))) {
      return Number(u.id);
    }
    return 0;
  }

  public getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  public isLoggedIn(): boolean {
    return this.getCurrentUser() !== null && !!this.getToken();
  }

  public sendOtp(target: { phoneNumber?: string; email?: string } | string): Observable<SendOtpResponse> {
    const payload = typeof target === 'string'
      ? (target.includes('@') ? { email: target } : { phoneNumber: target })
      : target;
    return this.http.post<SendOtpResponse>(`${this.apiUrl}/send-otp`, payload);
  }

  public register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap((response) => {
        if (response.success && response.user) {
          localStorage.setItem('currentUser', JSON.stringify(response.user));
          if (response.token) {
            localStorage.setItem('authToken', response.token);
          }
          this.currentUserSubject.next(response.user);
        }
      })
    );
  }

  public login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap((response) => {
        if (response.success && response.user) {
          localStorage.setItem('currentUser', JSON.stringify(response.user));
          if (response.token) {
            localStorage.setItem('authToken', response.token);
          }
          this.currentUserSubject.next(response.user);
        }
      })
    );
  }

  public logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }
}
