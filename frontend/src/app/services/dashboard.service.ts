import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardData } from '../models/dashboard.model';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiBaseUrl}/api/dashboard`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getDashboardData(userId?: number, month?: number, year?: number): Observable<DashboardData> {
    const id = userId || this.authService.getUserId();
    let params = new HttpParams();
    if (month) {
      params = params.set('month', month.toString());
    }
    if (year) {
      params = params.set('year', year.toString());
    }
    return this.http.get<DashboardData>(`${this.apiUrl}/${id}`, { params });
  }
}
