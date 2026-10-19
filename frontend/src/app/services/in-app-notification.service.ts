import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AppNotification, NotificationType } from '../models/notification.model';
import { RecurringPaymentService } from './recurring-payment.service';
import { BudgetService } from './budget.service';
import { SplitwiseService } from './splitwise.service';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InAppNotificationService {
  private readonly STORAGE_KEY = 'finance_inapp_notifications';
  private readonly DISMISSED_KEY = 'finance_dismissed_notif_ids';

  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  private activeBannerSubject = new BehaviorSubject<AppNotification | null>(null);
  public activeBanner$ = this.activeBannerSubject.asObservable();

  private isDrawerOpenSubject = new BehaviorSubject<boolean>(false);
  public isDrawerOpen$ = this.isDrawerOpenSubject.asObservable();

  private systemPermissionSubject = new BehaviorSubject<NotificationPermission | 'unsupported'>('default');
  public systemPermission$ = this.systemPermissionSubject.asObservable();

  private bannerTimeout: any = null;

  constructor(
    private recurringService: RecurringPaymentService,
    private budgetService: BudgetService,
    private splitwiseService: SplitwiseService,
    private authService: AuthService
  ) {
    this.initSystemNotificationStatus();
    this.loadInitialNotifications();

    // Automatically check for alerts when user is logged in
    this.authService.currentUser$.subscribe((user) => {
      if (user && user.id) {
        this.evaluateFinancialAlerts(user.id);
      }
    });
  }

  private initSystemNotificationStatus(): void {
    if (typeof window !== 'undefined' && 'Notification' in window) {
      this.systemPermissionSubject.next(Notification.permission);
    } else {
      this.systemPermissionSubject.next('unsupported');
    }
  }

  // ==========================================
  // Public Controls & Drawer
  // ==========================================

  public openDrawer(): void {
    this.isDrawerOpenSubject.next(true);
  }

  public closeDrawer(): void {
    this.isDrawerOpenSubject.next(false);
  }

  public toggleDrawer(): void {
    this.isDrawerOpenSubject.next(!this.isDrawerOpenSubject.value);
  }

  public dismissBanner(): void {
    if (this.bannerTimeout) {
      clearTimeout(this.bannerTimeout);
      this.bannerTimeout = null;
    }
    this.activeBannerSubject.next(null);
  }

  // ==========================================
  // Notification Management
  // ==========================================

  public markAsRead(id: string): void {
    const list = this.notificationsSubject.value.map(n => {
      if (n.id === id) {
        return { ...n, read: true };
      }
      return n;
    });
    this.saveAndPublish(list);
  }

  public markAllAsRead(): void {
    const list = this.notificationsSubject.value.map(n => ({ ...n, read: true }));
    this.saveAndPublish(list);
  }

  public dismissNotification(id: string): void {
    // Record as dismissed so it doesn't immediately reappear on re-evaluation
    this.recordDismissedId(id);
    const list = this.notificationsSubject.value.filter(n => n.id !== id);
    this.saveAndPublish(list);
  }

  public clearAll(): void {
    this.notificationsSubject.value.forEach(n => this.recordDismissedId(n.id));
    this.saveAndPublish([]);
  }

  public sendInAppNotification(notif: Omit<AppNotification, 'id' | 'date' | 'read'>): AppNotification {
    const newNotif: AppNotification = {
      ...notif,
      id: 'custom-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7),
      date: new Date().toISOString(),
      read: false
    };

    const current = this.notificationsSubject.value;
    const updated = [newNotif, ...current];
    this.saveAndPublish(updated);

    this.showHeadsUpBanner(newNotif);
    return newNotif;
  }

  public triggerTestNotification(): void {
    this.sendInAppNotification({
      title: '🔔 Notification System Active',
      body: 'In-app financial notifications are working on your device! You will receive alerts for upcoming bills, budget limits, and Splitwise balances.',
      type: 'SYSTEM',
      url: '/dashboard'
    });
  }

  public async requestSystemPermission(): Promise<NotificationPermission | 'unsupported'> {
    if (typeof window === 'undefined' || !('Notification' in window)) {
      this.systemPermissionSubject.next('unsupported');
      return 'unsupported';
    }

    try {
      const permission = await Notification.requestPermission();
      this.systemPermissionSubject.next(permission);

      if (permission === 'granted') {
        this.dispatchSystemNotification({
          id: 'system-enabled-' + Date.now(),
          title: '📲 Phone Notifications Active',
          body: 'System alerts enabled! You will now receive lock-screen & status-bar alerts for bills, rent, budgets, and Splitwise debts.',
          type: 'SYSTEM',
          date: new Date().toISOString(),
          read: false,
          url: '/dashboard'
        });
      }
      return permission;
    } catch (err) {
      console.warn('Notification permission request error:', err);
      return 'denied';
    }
  }

  public triggerTestSystemNotification(): void {
    const notif = this.sendInAppNotification({
      title: '⚡ Phone Alert: Rent Due Soon',
      body: 'Your apartment rent ₹12,000 is due on 2026-10-20. Tap to review payment.',
      type: 'BILL',
      url: '/recurring'
    });
    this.dispatchSystemNotification(notif);
  }

  // ==========================================
  // Smart Financial Alerts Evaluation
  // ==========================================

  public evaluateFinancialAlerts(userId: number): void {
    forkJoin({
      upcomingBills: this.recurringService.getUpcomingPayments(userId, 7).pipe(catchError(() => of([]))),
      budgets: this.budgetService.getBudgetsByUserId(userId).pipe(catchError(() => of([]))),
      groups: this.splitwiseService.getGroups(userId).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ upcomingBills, budgets, groups }) => {
        const generated: AppNotification[] = [];
        const dismissed = this.getDismissedIds();

        // 1. Evaluate Bills due soon (<= 3 days)
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        for (const bill of upcomingBills) {
          if (!bill.active) continue;
          const due = new Date(bill.nextDueDate);
          due.setHours(0, 0, 0, 0);
          const diffDays = Math.round((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

          if (diffDays <= 3) {
            const notifId = `bill-${bill.id}-${bill.nextDueDate}`;
            if (!dismissed.has(notifId)) {
              let dueText = diffDays === 0 ? 'due Today' : (diffDays === 1 ? 'due Tomorrow' : `due in ${diffDays} days`);
              generated.push({
                id: notifId,
                title: `⏰ Bill Reminder: ${bill.title}`,
                body: `Payment of ₹${bill.amount.toLocaleString()} is ${dueText} (${bill.nextDueDate}).`,
                type: 'BILL',
                date: new Date().toISOString(),
                read: false,
                url: '/recurring',
                amount: bill.amount
              });
            }
          }
        }

        // 2. Evaluate Budgets (>= 85% spent)
        for (const b of budgets) {
          if (b.progressPercentage >= 85) {
            const notifId = `budget-${b.id}-${b.month}-${b.year}`;
            if (!dismissed.has(notifId)) {
              const isOver = b.spentAmount > b.amount;
              generated.push({
                id: notifId,
                title: isOver ? `🚨 Budget Exceeded: ${b.categoryName}` : `⚠️ Budget Alert: ${b.categoryName}`,
                body: isOver
                  ? `You've exceeded your monthly limit! Spent ₹${b.spentAmount.toLocaleString()} of ₹${b.amount.toLocaleString()} (${b.progressPercentage.toFixed(0)}%).`
                  : `You've used ${b.progressPercentage.toFixed(0)}% of your monthly limit (₹${b.spentAmount.toLocaleString()} / ₹${b.amount.toLocaleString()}).`,
                type: 'BUDGET',
                date: new Date().toISOString(),
                read: false,
                url: '/budgets',
                amount: b.spentAmount
              });
            }
          }
        }

        // 3. Evaluate Splitwise balances
        for (const g of groups) {
          const net = g.netBalanceForCurrentUser || 0;
          if (Math.abs(net) >= 1.0) {
            const notifId = `split-${g.id}-${Math.round(net)}`;
            if (!dismissed.has(notifId)) {
              if (net > 0) {
                generated.push({
                  id: notifId,
                  title: `💸 You are owed ₹${net.toLocaleString()}`,
                  body: `Friends in group "${g.name}" owe you ₹${net.toLocaleString()}. Check settlement plan.`,
                  type: 'SPLITWISE',
                  date: new Date().toISOString(),
                  read: false,
                  url: '/splitwise',
                  amount: net
                });
              } else {
                generated.push({
                  id: notifId,
                  title: `💸 You owe ₹${Math.abs(net).toLocaleString()}`,
                  body: `You have an unsettled balance of ₹${Math.abs(net).toLocaleString()} in "${g.name}". Tap to settle up.`,
                  type: 'SPLITWISE',
                  date: new Date().toISOString(),
                  read: false,
                  url: '/splitwise',
                  amount: Math.abs(net)
                });
              }
            }
          }
        }

        if (generated.length > 0) {
          this.mergeGeneratedNotifications(generated);
        }
      }
    });
  }

  // ==========================================
  // Internal Helpers
  // ==========================================

  private loadInitialNotifications(): void {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (stored) {
        const parsed: AppNotification[] = JSON.parse(stored);
        this.notificationsSubject.next(parsed);
        this.updateUnreadCount(parsed);
        return;
      }
    } catch (e) {
      // ignore
    }

    // Default welcome notification if empty
    const welcomeNotif: AppNotification = {
      id: 'welcome-finance-tracker',
      title: '✨ In-App Notifications Active',
      body: 'Welcome! Important alerts regarding upcoming rent, subscription due dates, budget thresholds, and Splitwise debts will appear here.',
      type: 'SYSTEM',
      date: new Date().toISOString(),
      read: false,
      url: '/dashboard'
    };

    this.saveAndPublish([welcomeNotif]);
  }

  private mergeGeneratedNotifications(newOnes: AppNotification[]): void {
    const current = this.notificationsSubject.value;
    const existingIds = new Set(current.map(n => n.id));

    const newlyAdded: AppNotification[] = [];
    for (const n of newOnes) {
      if (!existingIds.has(n.id)) {
        newlyAdded.push(n);
      }
    }

    if (newlyAdded.length > 0) {
      const merged = [...newlyAdded, ...current];
      this.saveAndPublish(merged);

      // Pop the most urgent newly added notification as a floating banner
      this.showHeadsUpBanner(newlyAdded[0]);
    }
  }

  private showHeadsUpBanner(notif: AppNotification): void {
    if (this.bannerTimeout) {
      clearTimeout(this.bannerTimeout);
    }
    this.activeBannerSubject.next(notif);
    this.bannerTimeout = setTimeout(() => {
      this.activeBannerSubject.next(null);
      this.bannerTimeout = null;
    }, 5000);

    // Also dispatch to phone system notification tray if user granted permission
    this.dispatchSystemNotification(notif);
  }

  public async dispatchSystemNotification(notif: AppNotification): Promise<void> {
    if (typeof window === 'undefined' || !('Notification' in window) || Notification.permission !== 'granted') {
      return;
    }

    try {
      if ('serviceWorker' in navigator) {
        const reg = await navigator.serviceWorker.ready;
        if (reg && reg.showNotification) {
          await reg.showNotification(notif.title, {
            body: notif.body,
            icon: '/assets/icons/icon-192.png',
            badge: '/assets/icons/icon-192.png',
            tag: notif.id,
            vibrate: [200, 100, 200],
            data: { url: notif.url || '/dashboard' }
          } as any);
          return;
        }
      }

      // Fallback for desktop browsers without service worker
      new Notification(notif.title, {
        body: notif.body,
        icon: '/assets/icons/icon-192.png',
        tag: notif.id
      });
    } catch (err) {
      console.warn('Could not dispatch system notification:', err);
    }
  }

  private saveAndPublish(list: AppNotification[]): void {
    this.notificationsSubject.next(list);
    this.updateUnreadCount(list);
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(list));
    } catch (e) {
      // ignore
    }
  }

  private updateUnreadCount(list: AppNotification[]): void {
    const unread = list.filter(n => !n.read).length;
    this.unreadCountSubject.next(unread);
  }

  private getDismissedIds(): Set<string> {
    try {
      const raw = localStorage.getItem(this.DISMISSED_KEY);
      if (raw) {
        return new Set(JSON.parse(raw));
      }
    } catch (e) {
      // ignore
    }
    return new Set<string>();
  }

  private recordDismissedId(id: string): void {
    const set = this.getDismissedIds();
    set.add(id);
    try {
      localStorage.setItem(this.DISMISSED_KEY, JSON.stringify(Array.from(set)));
    } catch (e) {
      // ignore
    }
  }
}
