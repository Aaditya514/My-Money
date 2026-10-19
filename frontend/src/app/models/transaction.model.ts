export type TransactionType = 'INCOME' | 'EXPENSE' | 'REFUND';

export interface Transaction {
  id: number;
  title: string;
  amount: number;
  type: TransactionType;
  description?: string;
  transactionDate: string;
  categoryId: number;
  categoryName: string;
  accountId?: number;
  accountName?: string;
  userId: number;
  createdAt?: string;
}

export interface TransactionRequest {
  title: string;
  amount: number;
  type: TransactionType;
  description?: string;
  transactionDate: string;
  categoryId: number;
  accountId?: number;
  userId: number;
}
