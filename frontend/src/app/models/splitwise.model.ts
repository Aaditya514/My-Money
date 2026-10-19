export interface SplitMember {
  id: number;
  name: string;
  emailOrPhone?: string;
  currentUser: boolean;
  groupId?: number;
  totalPaid?: number;
  totalOwed?: number;
  netBalance?: number;
}

export interface SplitMemberRequest {
  name: string;
  emailOrPhone?: string;
  currentUser?: boolean;
}

export interface SplitGroup {
  id: number;
  name: string;
  description?: string;
  groupType: 'TRIP' | 'HOME' | 'OFFICE' | 'COUPLE' | 'OTHER' | string;
  memberCount: number;
  expenseCount: number;
  netBalanceForCurrentUser: number;
  members: SplitMember[];
  createdAt: string;
  updatedAt: string;
}

export interface SplitGroupRequest {
  name: string;
  description?: string;
  groupType?: string;
  memberNames?: string[];
  userId?: number;
}

export interface SplitExpenseShare {
  id?: number;
  memberId: number;
  memberName?: string;
  currentUser?: boolean;
  owedAmount: number;
  percentage?: number;
}

export interface SplitExpenseRequest {
  title: string;
  totalAmount: number;
  expenseDate?: string;
  paidByMemberId: number;
  splitMethod: 'EQUAL' | 'EXACT' | 'PERCENT';
  categoryId?: number;
  notes?: string;
  shares?: SplitExpenseShare[];
  userId?: number;
}

export interface SplitExpenseResponse {
  id: number;
  title: string;
  totalAmount: number;
  expenseDate: string;
  groupId: number;
  groupName: string;
  paidByMemberId: number;
  paidByMemberName: string;
  paidByCurrentUser: boolean;
  splitMethod: string;
  categoryId?: number;
  categoryName?: string;
  categoryColor?: string;
  syncedToLedger: boolean;
  ledgerTransactionId?: number;
  notes?: string;
  createdAt: string;
  shares: SplitExpenseShare[];
  userShareAmount: number;
  userNetEffect: number;
  statusText?: string;
}

export interface SplitSettlementRequest {
  paidByMemberId: number;
  paidToMemberId: number;
  amount: number;
  settlementDate?: string;
  notes?: string;
  recordInLedger?: boolean;
  categoryId?: number;
  userId?: number;
}

export interface SplitSettlementResponse {
  id: number;
  groupId: number;
  groupName: string;
  paidByMemberId: number;
  paidByMemberName: string;
  paidByCurrentUser: boolean;
  paidToMemberId: number;
  paidToMemberName: string;
  paidToCurrentUser: boolean;
  amount: number;
  settlementDate: string;
  notes?: string;
  recordedInLedger: boolean;
  ledgerTransactionId?: number;
  createdAt: string;
  narrative: string;
}

export interface DebtTransfer {
  fromMemberId: number;
  fromMemberName: string;
  fromCurrentUser: boolean;
  toMemberId: number;
  toMemberName: string;
  toCurrentUser: boolean;
  amount: number;
  narrative: string;
}

export interface SplitBalanceSummary {
  groupId: number;
  groupName: string;
  totalGroupSpending: number;
  userTotalPaid: number;
  userTotalShare: number;
  userNetBalance: number;
  userStatusHeadline: string;
  memberBalances: SplitMember[];
  simplifiedDebts: DebtTransfer[];
}

export interface SplitContact {
  name: string;
  phoneNumber?: string;
  email?: string;
  nickname?: string;
  registeredUserId?: number;
  groupsCount?: number;
  selected?: boolean;
}

