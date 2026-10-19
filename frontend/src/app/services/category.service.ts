import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, CategoryRequest, CategoryType } from '../models/category.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = `${environment.apiBaseUrl}/api/categories`;

  constructor(private http: HttpClient) {}

  getCategories(type?: CategoryType, userId?: number, includeArchived: boolean = false): Observable<Category[]> {
    let params = new HttpParams();
    if (type) {
      params = params.set('type', type);
    }
    if (userId) {
      params = params.set('userId', userId.toString());
    }
    if (includeArchived) {
      params = params.set('includeArchived', 'true');
    }
    return this.http.get<Category[]>(this.apiUrl, { params });
  }

  createCategory(category: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, category);
  }

  updateCategory(id: number, category: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, category);
  }

  archiveCategory(id: number, archived: boolean = true): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}/archive?archived=${archived}`, {});
  }

  reorderCategories(categoryIds: number[]): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/reorder`, categoryIds);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
