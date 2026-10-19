import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { InAppNotificationService } from '../services/in-app-notification.service';
import { DialogService } from '../services/dialog.service';
import { ThemeService } from '../services/theme.service';
import { User } from '../models/user.model';
import { Observable, filter } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  currentUser: User | null = null;
  currentUrl: string = '';
  unreadCount$: Observable<number>;
  isDarkMode$: Observable<boolean>;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notifService: InAppNotificationService,
    private dialogService: DialogService,
    public themeService: ThemeService
  ) {
    this.unreadCount$ = this.notifService.unreadCount$;
    this.isDarkMode$ = this.themeService.isDarkMode$;

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
    });

    this.currentUrl = this.router.url;

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.currentUrl = event.urlAfterRedirects;
      });
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAddPage(): boolean {
    return this.currentUrl.includes('/transactions/add') || this.currentUrl.includes('/transactions/edit');
  }

  openNotifications(): void {
    this.notifService.openDrawer();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  async onLogout(): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Sign Out',
      message: 'Are you sure you want to sign out of your account on this device?',
      confirmText: 'Sign Out',
      cancelText: 'Stay Logged In',
      type: 'warning',
      icon: 'logout'
    });

    if (confirmed) {
      this.authService.logout();
    }
  }
}
