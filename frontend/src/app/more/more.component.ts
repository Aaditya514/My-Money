import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { InAppNotificationService } from '../services/in-app-notification.service';
import { AiService } from '../services/ai.service';
import { DialogService } from '../services/dialog.service';
import { ThemeService, AppTheme } from '../services/theme.service';
import { Observable } from 'rxjs';

import { User } from '../models/user.model';

@Component({
  selector: 'app-more',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './more.component.html',
  styleUrls: ['./more.component.css']
})
export class MoreComponent implements OnInit {
  currentUser: User | null = null;
  deferredPrompt: any = null;
  isInstalled: boolean = false;
  unreadCount$: Observable<number>;
  currentTheme$: Observable<AppTheme>;

  // Three dots menu & Gemini API Key state
  showMenu: boolean = false;
  showKeyModal: boolean = false;
  apiKeyInput: string = '';
  showKeyText: boolean = false;
  testingKey: boolean = false;
  savingKey: boolean = false;
  testStatus: { success: boolean; message: string } | null = null;
  keyStatus: { configured: boolean; maskedKey: string } | null = null;
  hasCustomKey: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notifService: InAppNotificationService,
    private aiService: AiService,
    private dialogService: DialogService,
    public themeService: ThemeService
  ) {
    this.unreadCount$ = this.notifService.unreadCount$;
    this.currentTheme$ = this.themeService.currentTheme$;
  }

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((u) => {
      this.currentUser = u;
    });
    this.currentUser = this.authService.getCurrentUser();
    this.loadKeyStatus();

    // Check if running in standalone mode (already installed PWA)
    if (window.matchMedia('(display-mode: standalone)').matches || (window.navigator as any).standalone === true) {
      this.isInstalled = true;
    }

    // Listen for PWA install prompt
    window.addEventListener('beforeinstallprompt', (e: Event) => {
      e.preventDefault();
      this.deferredPrompt = e;
    });

    window.addEventListener('appinstalled', () => {
      this.isInstalled = true;
      this.deferredPrompt = null;
    });
  }

  loadKeyStatus(): void {
    const stored = this.aiService.getStoredApiKey();
    this.hasCustomKey = !!stored;
    if (stored) {
      this.apiKeyInput = stored;
    }
    this.aiService.getKeyStatus().subscribe({
      next: (res) => {
        this.keyStatus = res;
      },
      error: () => {}
    });
  }

  toggleMenu(): void {
    this.showMenu = !this.showMenu;
  }

  closeMenu(): void {
    this.showMenu = false;
  }

  openKeyModal(): void {
    this.showMenu = false;
    this.testStatus = null;
    const stored = this.aiService.getStoredApiKey();
    this.apiKeyInput = stored || '';
    this.showKeyModal = true;
    this.loadKeyStatus();
  }

  closeKeyModal(): void {
    this.showKeyModal = false;
    this.testStatus = null;
  }

  toggleKeyText(): void {
    this.showKeyText = !this.showKeyText;
  }

  testApiKey(): void {
    const key = this.apiKeyInput.trim();
    if (!key) {
      this.testStatus = { success: false, message: 'Please enter a Gemini API key first.' };
      return;
    }
    this.testingKey = true;
    this.testStatus = null;
    this.aiService.testApiKey(key).subscribe({
      next: (res) => {
        this.testingKey = false;
        if (res.valid) {
          this.testStatus = {
            success: true,
            message: `Key verified successfully! Connected to Gemini (${res.model || 'gemini-1.5-flash'}).`
          };
        } else {
          this.testStatus = { success: false, message: res.message || 'Key test failed.' };
        }
      },
      error: (err) => {
        this.testingKey = false;
        this.testStatus = {
          success: false,
          message: err.error?.message || 'Failed to verify key with Gemini API. Check your internet connection.'
        };
      }
    });
  }

  saveApiKey(): void {
    const key = this.apiKeyInput.trim();
    if (!key) {
      this.testStatus = { success: false, message: 'Please enter an API key to save.' };
      return;
    }
    this.savingKey = true;
    this.aiService.setStoredApiKey(key);
    this.hasCustomKey = true;

    this.aiService.updateServerKey(key).subscribe({
      next: () => {
        this.savingKey = false;
        this.testStatus = { success: true, message: 'Gemini API Key saved and active for all scans!' };
        this.loadKeyStatus();
        setTimeout(() => this.closeKeyModal(), 1200);
      },
      error: () => {
        this.savingKey = false;
        this.testStatus = { success: true, message: 'Key saved locally in your browser/device!' };
        this.loadKeyStatus();
        setTimeout(() => this.closeKeyModal(), 1200);
      }
    });
  }

  setTheme(theme: AppTheme): void {
    this.themeService.setTheme(theme);
  }

  async resetApiKey(): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Reset Gemini API Key',
      message: 'Reset your Gemini API key to the system default configuration? Any custom quota will revert to the default system key.',
      confirmText: 'Reset to Default',
      cancelText: 'Keep Custom Key',
      type: 'warning',
      icon: 'key'
    });

    if (confirmed) {
      this.aiService.clearStoredApiKey();
      this.apiKeyInput = '';
      this.hasCustomKey = false;
      this.testStatus = { success: true, message: 'Reset to default API key.' };
      this.loadKeyStatus();
    }
  }

  async installPwa(): Promise<void> {
    if (this.deferredPrompt) {
      this.deferredPrompt.prompt();
      this.deferredPrompt.userChoice.then((choiceResult: any) => {
        if (choiceResult.outcome === 'accepted') {
          this.isInstalled = true;
        }
        this.deferredPrompt = null;
      });
    } else {
      await this.dialogService.alert({
        title: 'Install on Your Phone',
        message: 'To install Finance Tracker on your phone as an app:\n\n• Android (Chrome): Tap the top right menu (3 dots), then tap "Install app" or "Add to Home screen".\n• iPhone (Safari): Tap the Share button at the bottom, then tap "Add to Home Screen".',
        confirmText: 'Got It',
        type: 'info'
      });
    }
  }

  openNotifications(): void {
    this.notifService.openDrawer();
  }

  async onLogout(): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Sign Out',
      message: 'Are you sure you want to sign out of Finance Tracker on this device?',
      confirmText: 'Sign Out',
      cancelText: 'Stay Logged In',
      type: 'warning',
      icon: 'logout'
    });

    if (confirmed) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }
}

