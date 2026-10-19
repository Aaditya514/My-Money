import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { InAppNotificationService } from '../services/in-app-notification.service';
import { AppNotification, NotificationType } from '../models/notification.model';

@Component({
  selector: 'app-notification-center',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-center.component.html',
  styleUrls: ['./notification-center.component.css']
})
export class NotificationCenterComponent implements OnInit, OnDestroy {
  isOpen: boolean = false;
  activeBanner: AppNotification | null = null;
  notifications: AppNotification[] = [];
  unreadCount: number = 0;
  activeFilter: 'ALL' | NotificationType = 'ALL';
  systemPermission: NotificationPermission | 'unsupported' = 'default';

  private subs = new Subscription();

  constructor(
    private notifService: InAppNotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.notifService.isDrawerOpen$.subscribe(open => {
        this.isOpen = open;
      })
    );

    this.subs.add(
      this.notifService.activeBanner$.subscribe(banner => {
        this.activeBanner = banner;
      })
    );

    this.subs.add(
      this.notifService.notifications$.subscribe(list => {
        this.notifications = list;
      })
    );

    this.subs.add(
      this.notifService.unreadCount$.subscribe(count => {
        this.unreadCount = count;
      })
    );

    this.subs.add(
      this.notifService.systemPermission$.subscribe(perm => {
        this.systemPermission = perm;
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  get filteredNotifications(): AppNotification[] {
    if (this.activeFilter === 'ALL') {
      return this.notifications;
    }
    return this.notifications.filter(n => n.type === this.activeFilter);
  }

  close(): void {
    this.notifService.closeDrawer();
  }

  dismissBanner(): void {
    this.notifService.dismissBanner();
  }

  async enablePhoneNotifications(): Promise<void> {
    await this.notifService.requestSystemPermission();
  }

  onSendPhoneTest(): void {
    this.notifService.triggerTestSystemNotification();
  }

  onNotificationClick(notif: AppNotification): void {
    this.notifService.markAsRead(notif.id);
    this.close();
    this.dismissBanner();
    if (notif.url) {
      this.router.navigateByUrl(notif.url);
    }
  }

  onDismiss(id: string, event: Event): void {
    event.stopPropagation();
    this.notifService.dismissNotification(id);
  }

  onMarkAllAsRead(): void {
    this.notifService.markAllAsRead();
  }

  onClearAll(): void {
    this.notifService.clearAll();
  }

  onSendTest(): void {
    this.notifService.triggerTestNotification();
  }

  getTypeBadge(type: NotificationType): { label: string; class: string } {
    switch (type) {
      case 'BILL':
        return { label: 'Bills & Rent', class: 'badge-bill' };
      case 'BUDGET':
        return { label: 'Budget Alert', class: 'badge-budget' };
      case 'SPLITWISE':
        return { label: 'Splitwise', class: 'badge-splitwise' };
      default:
        return { label: 'System', class: 'badge-system' };
    }
  }

  getTimeAgo(dateStr: string): string {
    const diff = Date.now() - new Date(dateStr).getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days === 1) return 'Yesterday';
    return `${days}d ago`;
  }
}
