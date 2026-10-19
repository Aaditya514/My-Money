import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type AppTheme = 'light' | 'dark' | 'system';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_STORAGE_KEY = 'finance_tracker_theme';
  private themeSubject: BehaviorSubject<AppTheme>;
  public currentTheme$: Observable<AppTheme>;

  private isDarkSubject: BehaviorSubject<boolean>;
  public isDarkMode$: Observable<boolean>;

  private mediaQuery: MediaQueryList;

  constructor() {
    this.mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const savedTheme = this.getSavedTheme();
    
    this.themeSubject = new BehaviorSubject<AppTheme>(savedTheme);
    this.currentTheme$ = this.themeSubject.asObservable();

    const initialIsDark = this.resolveIsDark(savedTheme);
    this.isDarkSubject = new BehaviorSubject<boolean>(initialIsDark);
    this.isDarkMode$ = this.isDarkSubject.asObservable();

    // Apply initially
    this.applyTheme(savedTheme);

    // Listen to OS system theme changes
    this.mediaQuery.addEventListener('change', (e) => {
      if (this.themeSubject.value === 'system') {
        this.updateDOM(e.matches);
        this.isDarkSubject.next(e.matches);
      }
    });
  }

  get theme(): AppTheme {
    return this.themeSubject.value;
  }

  get isDark(): boolean {
    return this.isDarkSubject.value;
  }

  setTheme(theme: AppTheme): void {
    try {
      localStorage.setItem(this.THEME_STORAGE_KEY, theme);
    } catch {
      // localStorage may fail in private mode
    }
    this.themeSubject.next(theme);
    this.applyTheme(theme);
  }

  toggleTheme(): void {
    const nextTheme: AppTheme = this.isDark ? 'light' : 'dark';
    this.setTheme(nextTheme);
  }

  private getSavedTheme(): AppTheme {
    try {
      const stored = localStorage.getItem(this.THEME_STORAGE_KEY) as AppTheme;
      if (stored === 'light' || stored === 'dark' || stored === 'system') {
        return stored;
      }
    } catch {
      // ignore
    }
    return 'system';
  }

  private resolveIsDark(theme: AppTheme): boolean {
    if (theme === 'dark') return true;
    if (theme === 'light') return false;
    return this.mediaQuery.matches;
  }

  private applyTheme(theme: AppTheme): void {
    const isDark = this.resolveIsDark(theme);
    this.isDarkSubject.next(isDark);
    this.updateDOM(isDark);
  }

  private updateDOM(isDark: boolean): void {
    const root = document.documentElement;
    const body = document.body;
    if (isDark) {
      root.setAttribute('data-theme', 'dark');
      body.setAttribute('data-theme', 'dark');
      root.classList.add('dark-theme');
      body.classList.add('dark-theme');
    } else {
      root.setAttribute('data-theme', 'light');
      body.setAttribute('data-theme', 'light');
      root.classList.remove('dark-theme');
      body.classList.remove('dark-theme');
    }
  }
}
