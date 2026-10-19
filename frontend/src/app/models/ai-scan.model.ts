import { TransactionType } from './transaction.model';

export interface AiBillScanResponse {
  title: string;
  amount: number;
  type: TransactionType;
  categoryId: number | null;
  categoryName: string;
  transactionDate: string;
  description: string;
  rawSummary: string;
}

export interface AiTextScanRequest {
  statement: string;
}

export interface GroupMemberContext {
  id: number;
  name: string;
}

export interface GroupMemberShareDto {
  memberId: number;
  memberName: string;
  owedAmount: number;
  itemsSummary: string;
}

export interface BillItemDto {
  name: string;
  price: number;
  assignedTo: string[];
}

export interface AiGroupBillScanResponse {
  merchantName: string;
  expenseTitle: string;
  totalAmount: number;
  subtotal: number;
  taxAmount: number;
  tipOrServiceAmount: number;
  categoryId: number | null;
  categoryName: string;
  expenseDate: string;
  paidByMemberId: number | null;
  paidByMemberName: string;
  notes: string;
  memberShares: GroupMemberShareDto[];
  items: BillItemDto[];
}

export interface AiGroupBillScanRequest {
  statement: string;
  orderNotes?: string;
  defaultPayerMemberId?: number;
  groupMembers: GroupMemberContext[];
}
