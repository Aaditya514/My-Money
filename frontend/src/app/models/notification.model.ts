export type NotificationType = 'BILL' | 'BUDGET' | 'SPLITWISE' | 'SYSTEM';

export interface AppNotification {
  id: string;
  title: string;
  body: string;
  type: NotificationType;
  date: string; // ISO string
  read: boolean;
  url: string;
  amount?: number;
}

export interface NotificationPreferences {
  bills: boolean;
  budgets: boolean;
  splitwise: boolean;
  sound: boolean;
}
