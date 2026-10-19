import { TransactionType } from './transaction.model';

export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'BI_WEEKLY' | 'MONTHLY' | 'QUARTERLY' | 'YEARLY';

export interface RecurringPayment {
  id: number;
  title: string;
  amount: number;
  type: TransactionType;
  categoryId: number;
  categoryName?: string;
  frequency: RecurrenceFrequency;
  startDate: string;
  nextDueDate: string;
  active: boolean;
  notes?: string;
  userId: number;
  createdAt?: string;
  updatedAt?: string;
  daysUntilDue?: number;
  overdue?: boolean;
}

export interface RecurringPaymentRequest {
  title: string;
  amount: number;
  type?: TransactionType;
  categoryId: number;
  frequency: RecurrenceFrequency;
  startDate: string;
  nextDueDate?: string;
  notes?: string;
  userId: number;
  active?: boolean;
}

export interface RecurringPaymentRecordResult {
  recurringPayment: RecurringPayment;
  transaction: any;
  message: string;
}
