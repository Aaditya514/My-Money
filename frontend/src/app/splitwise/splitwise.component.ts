import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { SplitwiseService } from '../services/splitwise.service';
import { CategoryService } from '../services/category.service';
import { AuthService } from '../services/auth.service';
import { DialogService } from '../services/dialog.service';
import { AiService } from '../services/ai.service';
import {
  DebtTransfer,
  SplitBalanceSummary,
  SplitExpenseRequest,
  SplitExpenseResponse,
  SplitExpenseShare,
  SplitGroup,
  SplitGroupRequest,
  SplitMember,
  SplitMemberRequest,
  SplitSettlementRequest,
  SplitSettlementResponse,
  SplitContact
} from '../models/splitwise.model';
import {
  AiGroupBillScanResponse,
  GroupMemberContext
} from '../models/ai-scan.model';
import { Category, CategoryType } from '../models/category.model';

@Component({
  selector: 'app-splitwise',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './splitwise.component.html',
  styleUrls: ['./splitwise.component.css']
})
export class SplitwiseComponent implements OnInit {
  userId: number = 1;
  userName: string = 'You';

  groups: SplitGroup[] = [];
  selectedGroup: SplitGroup | null = null;
  selectedGroupId: number | null = null;

  activeTab: 'expenses' | 'balances' | 'settlements' = 'expenses';

  expenses: SplitExpenseResponse[] = [];
  balanceSummary: SplitBalanceSummary | null = null;
  settlements: SplitSettlementResponse[] = [];
  categories: Category[] = [];

  isLoading: boolean = false;
  toastMessage: string | null = null;
  toastType: 'success' | 'error' | 'info' = 'success';

  // Modals state
  showAddGroupModal: boolean = false;
  showAddExpenseModal: boolean = false;
  showSettleModal: boolean = false;
  showAddMemberModal: boolean = false;
  showAiSplitModal: boolean = false;
  showContactsModal: boolean = false;
  showEditNicknameModal: boolean = false;

  // Contacts Book state
  contactsList: SplitContact[] = [];
  filteredContacts: SplitContact[] = [];
  contactSearchQuery: string = '';
  editingNicknameContact: SplitContact | null = null;
  nicknameInput: string = '';
  userNicknames: Record<string, string> = {};
  isSearchingContacts: boolean = false;
  contactsSourceContext: 'addGroup' | 'addMember' | 'general' = 'addGroup';

  // AI Bill Splitter state
  aiActiveTab: 'image' | 'text' = 'image';
  aiSelectedFiles: File[] = [];
  aiPreviewUrls: string[] = [];
  aiStatementText: string = '';
  aiOrderNotes: string = '';
  aiPayerMemberId: number | null = null;
  isAiScanning: boolean = false;
  aiScanResult: AiGroupBillScanResponse | null = null;
  aiErrorMessage: string | null = null;

  // Add Group Form
  newGroup: SplitGroupRequest = {
    name: '',
    description: '',
    groupType: 'TRIP',
    memberNames: []
  };
  memberNamesInput: string = '';

  // Add Expense Form
  newExpense: {
    title: string;
    totalAmount: number | null;
    expenseDate: string;
    paidByMemberId: number | null;
    splitMethod: 'EQUAL' | 'EXACT' | 'PERCENT';
    categoryId: number | null;
    notes: string;
    customShares: { memberId: number; name: string; amount: number; percent: number; selected: boolean }[];
  } = {
    title: '',
    totalAmount: null,
    expenseDate: new Date().toISOString().substring(0, 10),
    paidByMemberId: null,
    splitMethod: 'EQUAL',
    categoryId: null,
    notes: '',
    customShares: []
  };

  // Settle Up Form
  settleForm: {
    paidByMemberId: number | null;
    paidToMemberId: number | null;
    amount: number | null;
    settlementDate: string;
    notes: string;
    recordInLedger: boolean;
  } = {
    paidByMemberId: null,
    paidToMemberId: null,
    amount: null,
    settlementDate: new Date().toISOString().substring(0, 10),
    notes: '',
    recordInLedger: true
  };

  // Add Member Form
  newMember: SplitMemberRequest = {
    name: '',
    emailOrPhone: '',
    currentUser: false
  };

  constructor(
    private splitwiseService: SplitwiseService,
    private categoryService: CategoryService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private dialogService: DialogService,
    private aiService: AiService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user && user.id) {
      this.userId = user.id;
      this.userName = user.name || 'You';
    }

    this.loadCategories();
    this.loadGroups();
  }

  loadCategories(): void {
    this.categoryService.getCategories('EXPENSE', this.userId).subscribe({
      next: (cats) => {
        this.categories = cats;
      },
      error: () => {}
    });
  }

  loadGroups(autoSelectGroupId?: number): void {
    this.isLoading = true;
    this.splitwiseService.getGroups(this.userId).subscribe({
      next: (groups) => {
        this.groups = groups;
        this.isLoading = false;

        if (groups.length > 0) {
          if (autoSelectGroupId) {
            const found = groups.find(g => g.id === autoSelectGroupId);
            this.selectGroup(found ? found : groups[0]);
          } else if (this.selectedGroupId) {
            const found = groups.find(g => g.id === this.selectedGroupId);
            this.selectGroup(found ? found : groups[0]);
          } else {
            this.selectGroup(groups[0]);
          }
        } else {
          this.selectedGroup = null;
          this.selectedGroupId = null;
          this.expenses = [];
          this.balanceSummary = null;
          this.settlements = [];
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to load split groups', 'error');
      }
    });
  }

  selectGroup(group: SplitGroup): void {
    this.selectedGroup = group;
    this.selectedGroupId = group.id;
    this.loadGroupDetails(group.id);
  }

  loadGroupDetails(groupId: number): void {
    this.splitwiseService.getExpenses(groupId, this.userId).subscribe({
      next: (exps) => {
        this.expenses = exps;
      }
    });

    this.splitwiseService.getBalanceSummary(groupId, this.userId).subscribe({
      next: (summary) => {
        this.balanceSummary = summary;
      }
    });

    this.splitwiseService.getSettlements(groupId, this.userId).subscribe({
      next: (settles) => {
        this.settlements = settles;
      }
    });
  }

  // ==========================================
  // Group Operations
  // ==========================================

  openAddGroup(): void {
    this.newGroup = {
      name: '',
      description: '',
      groupType: 'TRIP',
      memberNames: []
    };
    this.memberNamesInput = '';
    this.showAddGroupModal = true;
  }

  submitAddGroup(): void {
    if (!this.newGroup.name.trim()) {
      this.showToast('Group name is required', 'error');
      return;
    }

    const members = this.memberNamesInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    const req: SplitGroupRequest = {
      name: this.newGroup.name.trim(),
      description: this.newGroup.description,
      groupType: this.newGroup.groupType,
      memberNames: members,
      userId: this.userId
    };

    this.isLoading = true;
    this.splitwiseService.createGroup(req, this.userId).subscribe({
      next: (created) => {
        this.isLoading = false;
        this.showAddGroupModal = false;
        this.showToast(`Group "${created.name}" created successfully!`, 'success');
        this.loadGroups(created.id);
      },
      error: () => {
        this.isLoading = false;
        this.showToast('Error creating group', 'error');
      }
    });
  }

  async confirmDeleteGroup(): Promise<void> {
    if (!this.selectedGroup) return;
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Splitwise Group',
      message: `Are you sure you want to delete group "${this.selectedGroup.name}"? All expenses and balances in this group will be deleted.`,
      confirmText: 'Delete Group',
      cancelText: 'Keep Group',
      type: 'danger',
      icon: 'trash'
    });

    if (confirmed) {
      this.splitwiseService.deleteGroup(this.selectedGroup.id, this.userId).subscribe({
        next: () => {
          this.showToast('Group deleted', 'info');
          this.loadGroups();
        },
        error: () => {
          this.showToast('Failed to delete group', 'error');
        }
      });
    }
  }

  // ==========================================
  // Member Operations
  // ==========================================

  openAddMember(): void {
    this.newMember = {
      name: '',
      emailOrPhone: '',
      currentUser: false
    };
    this.showAddMemberModal = true;
  }

  submitAddMember(): void {
    if (!this.selectedGroup || !this.newMember.name.trim()) return;

    this.splitwiseService.addMember(this.selectedGroup.id, this.newMember, this.userId).subscribe({
      next: (m) => {
        this.showAddMemberModal = false;
        this.showToast(`Added ${m.name} to group`, 'success');
        this.loadGroups(this.selectedGroup!.id);
      },
      error: () => {
        this.showToast('Failed to add member', 'error');
      }
    });
  }

  // ==========================================
  // Expense Operations
  // ==========================================

  openAddExpense(): void {
    if (!this.selectedGroup) return;

    const members = this.selectedGroup.members || [];
    const currentUserMember = members.find(m => m.currentUser);

    this.newExpense = {
      title: '',
      totalAmount: null,
      expenseDate: new Date().toISOString().substring(0, 10),
      paidByMemberId: currentUserMember ? currentUserMember.id : (members.length > 0 ? members[0].id : null),
      splitMethod: 'EQUAL',
      categoryId: this.categories.length > 0 ? this.categories[0].id : null,
      notes: '',
      customShares: members.map(m => ({
        memberId: m.id,
        name: m.name,
        amount: 0,
        percent: members.length > 0 ? Math.round((100 / members.length) * 100) / 100 : 0,
        selected: true
      }))
    };

    this.showAddExpenseModal = true;
  }

  onTotalAmountChange(): void {
    if (this.newExpense.splitMethod === 'EQUAL') {
      const selectedCount = this.newExpense.customShares.filter(s => s.selected).length;
      if (selectedCount > 0 && this.newExpense.totalAmount) {
        const perPerson = Math.floor((this.newExpense.totalAmount / selectedCount) * 100) / 100;
        this.newExpense.customShares.forEach(s => {
          s.amount = s.selected ? perPerson : 0;
        });
      }
    }
  }

  submitAddExpense(): void {
    if (!this.selectedGroup) return;
    if (!this.newExpense.title.trim()) {
      this.showToast('Please enter an expense title', 'error');
      return;
    }
    if (!this.newExpense.totalAmount || this.newExpense.totalAmount <= 0) {
      this.showToast('Please enter a valid amount', 'error');
      return;
    }
    if (!this.newExpense.paidByMemberId) {
      this.showToast('Please choose who paid', 'error');
      return;
    }

    let shares: SplitExpenseShare[] = [];

    if (this.newExpense.splitMethod === 'EQUAL') {
      const active = this.newExpense.customShares.filter(s => s.selected);
      if (active.length === 0) {
        this.showToast('At least one member must be included in the split', 'error');
        return;
      }
      shares = active.map(s => ({
        memberId: s.memberId,
        owedAmount: 0 // Service will compute exact equal cents
      }));
    } else if (this.newExpense.splitMethod === 'EXACT') {
      const sum = this.newExpense.customShares.reduce((acc, s) => acc + (s.amount || 0), 0);
      if (Math.abs(sum - this.newExpense.totalAmount) > 0.05) {
        this.showToast(`Sum of shares (₹${sum.toFixed(2)}) must equal total (₹${this.newExpense.totalAmount.toFixed(2)})`, 'error');
        return;
      }
      shares = this.newExpense.customShares.map(s => ({
        memberId: s.memberId,
        owedAmount: s.amount || 0
      }));
    } else if (this.newExpense.splitMethod === 'PERCENT') {
      const sumPct = this.newExpense.customShares.reduce((acc, s) => acc + (s.percent || 0), 0);
      if (Math.abs(sumPct - 100) > 0.5) {
        this.showToast(`Sum of percentages (${sumPct.toFixed(1)}%) must equal 100%`, 'error');
        return;
      }
      shares = this.newExpense.customShares.map(s => ({
        memberId: s.memberId,
        owedAmount: 0,
        percentage: s.percent
      }));
    }

    const req: SplitExpenseRequest = {
      title: this.newExpense.title.trim(),
      totalAmount: this.newExpense.totalAmount,
      expenseDate: this.newExpense.expenseDate,
      paidByMemberId: this.newExpense.paidByMemberId,
      splitMethod: this.newExpense.splitMethod,
      categoryId: this.newExpense.categoryId || undefined,
      notes: this.newExpense.notes,
      shares: shares,
      userId: this.userId
    };

    this.isLoading = true;
    this.splitwiseService.createExpense(this.selectedGroup.id, req, this.userId).subscribe({
      next: (exp) => {
        this.isLoading = false;
        this.showAddExpenseModal = false;
        this.showToast(`Added expense "${exp.title}"`, 'success');
        this.loadGroupDetails(this.selectedGroup!.id);
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast('Failed to add expense', 'error');
      }
    });
  }

  async deleteExpense(exp: SplitExpenseResponse): Promise<void> {
    if (!this.selectedGroup) return;
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Expense',
      message: `Are you sure you want to delete "${exp.title}"? This will recalculate member balances.`,
      confirmText: 'Delete Expense',
      cancelText: 'Keep Expense',
      type: 'danger',
      icon: 'trash'
    });

    if (confirmed) {
      this.splitwiseService.deleteExpense(this.selectedGroup.id, exp.id, this.userId).subscribe({
        next: () => {
          this.showToast('Expense removed', 'info');
          this.loadGroupDetails(this.selectedGroup!.id);
        },
        error: () => {
          this.showToast('Failed to delete expense', 'error');
        }
      });
    }
  }

  // ==========================================
  // 1-Tap Sync to Personal Ledger
  // ==========================================

  syncToLedger(exp: SplitExpenseResponse): void {
    if (!this.selectedGroup || exp.syncedToLedger) return;

    this.splitwiseService.syncExpenseToLedger(this.selectedGroup.id, exp.id, this.userId).subscribe({
      next: (updated) => {
        exp.syncedToLedger = true;
        exp.ledgerTransactionId = updated.ledgerTransactionId;
        this.showToast(`Synced ₹${exp.userShareAmount.toFixed(2)} to your personal ledger!`, 'success');
      },
      error: (err) => {
        this.showToast('Failed to sync to ledger', 'error');
      }
    });
  }

  // ==========================================
  // Settlement Operations
  // ==========================================

  openSettleModal(prefill?: { fromId: number; toId: number; amount: number }): void {
    if (!this.selectedGroup) return;

    const members = this.selectedGroup.members || [];
    const currentUserMember = members.find(m => m.currentUser);

    if (prefill) {
      this.settleForm = {
        paidByMemberId: prefill.fromId,
        paidToMemberId: prefill.toId,
        amount: prefill.amount,
        settlementDate: new Date().toISOString().substring(0, 10),
        notes: 'Settled via Splitwise',
        recordInLedger: true
      };
    } else {
      this.settleForm = {
        paidByMemberId: currentUserMember ? currentUserMember.id : (members.length > 0 ? members[0].id : null),
        paidToMemberId: members.length > 1 ? members[1].id : null,
        amount: null,
        settlementDate: new Date().toISOString().substring(0, 10),
        notes: '',
        recordInLedger: true
      };
    }

    this.showSettleModal = true;
  }

  submitSettle(): void {
    if (!this.selectedGroup) return;
    if (!this.settleForm.paidByMemberId || !this.settleForm.paidToMemberId) {
      this.showToast('Please select both payer and recipient', 'error');
      return;
    }
    if (this.settleForm.paidByMemberId === this.settleForm.paidToMemberId) {
      this.showToast('Payer and recipient cannot be the same', 'error');
      return;
    }
    if (!this.settleForm.amount || this.settleForm.amount <= 0) {
      this.showToast('Please enter a valid settlement amount', 'error');
      return;
    }

    const req: SplitSettlementRequest = {
      paidByMemberId: this.settleForm.paidByMemberId,
      paidToMemberId: this.settleForm.paidToMemberId,
      amount: this.settleForm.amount,
      settlementDate: this.settleForm.settlementDate,
      notes: this.settleForm.notes,
      recordInLedger: this.settleForm.recordInLedger,
      userId: this.userId
    };

    this.isLoading = true;
    this.splitwiseService.createSettlement(this.selectedGroup.id, req, this.userId).subscribe({
      next: (settle) => {
        this.isLoading = false;
        this.showSettleModal = false;
        this.showToast(settle.narrative, 'success');
        this.loadGroupDetails(this.selectedGroup!.id);
      },
      error: () => {
        this.isLoading = false;
        this.showToast('Failed to record settlement', 'error');
      }
    });
  }

  showToast(message: string, type: 'success' | 'error' | 'info' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = null;
    }, 3800);
  }

  getGroupTypeIcon(type: string): string {
    switch ((type || '').toUpperCase()) {
      case 'TRIP': return '✈️';
      case 'HOME': return '🏠';
      case 'OFFICE': return '🏢';
      case 'COUPLE': return '❤️';
      default: return '👥';
    }
  }

  // ==========================================
  // AI Bill Splitting
  // ==========================================

  openAiSplitModal(): void {
    if (!this.selectedGroup) return;

    this.aiActiveTab = 'image';
    this.aiSelectedFiles = [];
    this.aiPreviewUrls = [];
    this.aiStatementText = '';
    this.aiOrderNotes = '';
    this.aiScanResult = null;
    this.aiErrorMessage = null;

    const members = this.selectedGroup.members || [];
    const currentUserMember = members.find(m => m.currentUser);
    this.aiPayerMemberId = currentUserMember ? currentUserMember.id : (members.length > 0 ? members[0].id : null);

    this.showAiSplitModal = true;
  }

  onAiFilesSelected(event: any): void {
    const files: FileList = event.target.files;
    if (!files || files.length === 0) return;

    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      if (file.type.startsWith('image/')) {
        this.aiSelectedFiles.push(file);

        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.aiPreviewUrls.push(e.target.result);
        };
        reader.readAsDataURL(file);
      }
    }
    event.target.value = '';
  }

  removeAiFile(index: number): void {
    if (index >= 0 && index < this.aiSelectedFiles.length) {
      this.aiSelectedFiles.splice(index, 1);
      this.aiPreviewUrls.splice(index, 1);
    }
  }

  appendMemberToNotes(memberName: string): void {
    const toAppend = this.aiOrderNotes && this.aiOrderNotes.trim().length > 0
      ? `\n${memberName}: `
      : `${memberName}: `;
    this.aiOrderNotes += toAppend;
  }

  runAiGroupSplit(): void {
    if (!this.selectedGroup) return;

    if (this.aiActiveTab === 'image' && this.aiSelectedFiles.length === 0) {
      this.aiErrorMessage = 'Please upload or capture at least one receipt image.';
      return;
    }
    if (this.aiActiveTab === 'text' && !this.aiStatementText.trim()) {
      this.aiErrorMessage = 'Please enter or paste the bill statement text.';
      return;
    }

    this.isAiScanning = true;
    this.aiErrorMessage = null;
    this.aiScanResult = null;

    const membersContext: GroupMemberContext[] = (this.selectedGroup.members || []).map(m => ({
      id: m.id,
      name: m.name
    }));

    const observable = (this.aiActiveTab === 'image')
      ? this.aiService.scanGroupReceiptImages(
          this.aiSelectedFiles,
          membersContext,
          this.aiOrderNotes,
          this.aiPayerMemberId || undefined
        )
      : this.aiService.scanGroupBillStatement(
          this.aiStatementText.trim(),
          membersContext,
          this.aiOrderNotes,
          this.aiPayerMemberId || undefined
        );

    observable.subscribe({
      next: (result) => {
        this.isAiScanning = false;
        this.aiScanResult = result;
        if (result.paidByMemberId) {
          this.aiPayerMemberId = result.paidByMemberId;
        }
      },
      error: (err) => {
        this.isAiScanning = false;
        const msg = err.error?.message || err.message || 'AI bill analysis failed. Please verify your Gemini API key in More Settings.';
        this.aiErrorMessage = msg;
      }
    });
  }

  applyAiResultToExpense(): void {
    if (!this.aiScanResult || !this.selectedGroup) return;

    const res = this.aiScanResult;
    const members = this.selectedGroup.members || [];

    const shareMap = new Map<number, number>();
    for (const s of (res.memberShares || [])) {
      shareMap.set(s.memberId, s.owedAmount);
    }

    const customShares = members.map(m => {
      const amount = shareMap.get(m.id) ?? 0;
      return {
        memberId: m.id,
        name: m.name,
        amount: amount,
        percent: res.totalAmount > 0 ? Math.round((amount / res.totalAmount) * 1000) / 10 : 0,
        selected: amount > 0
      };
    });

    this.newExpense = {
      title: res.expenseTitle || res.merchantName || 'Group Receipt',
      totalAmount: res.totalAmount,
      expenseDate: res.expenseDate || new Date().toISOString().substring(0, 10),
      paidByMemberId: res.paidByMemberId || this.aiPayerMemberId || (members.length > 0 ? members[0].id : null),
      splitMethod: 'EXACT',
      categoryId: res.categoryId || (this.categories.length > 0 ? this.categories[0].id : null),
      notes: res.notes || '',
      customShares: customShares
    };

    this.showAiSplitModal = false;
    this.showAddExpenseModal = true;
    this.showToast('AI calculation loaded! Review details and save.', 'success');
  }

  saveAiExpenseDirectly(): void {
    this.applyAiResultToExpense();
    this.submitAddExpense();
  }

  // ==========================================
  // Contacts Book & Custom Nickname Methods
  // ==========================================

  openContactsModal(context: 'addGroup' | 'addMember' | 'general' = 'addGroup'): void {
    this.contactsSourceContext = context;
    this.showContactsModal = true;
    this.contactSearchQuery = '';
    this.loadContacts();
  }

  closeContactsModal(): void {
    this.showContactsModal = false;
  }

  loadContacts(): void {
    const userId = this.userId || 1;
    this.userNicknames = this.splitwiseService.getUserNicknames(userId);

    // 1. Instant load from local cache (0ms latency)
    const cached = this.splitwiseService.getCachedContacts(userId);
    if (cached && cached.length > 0) {
      this.contactsList = cached.map(c => ({
        ...c,
        nickname: this.userNicknames[this.getContactKey(c)] || c.nickname || '',
        selected: false
      }));
      this.filteredContacts = [...this.contactsList];
    }

    // 2. Fetch fresh contacts from backend and persist in cache
    this.splitwiseService.getContacts(userId).subscribe({
      next: (contacts) => {
        const enriched = contacts.map(c => ({
          ...c,
          nickname: this.userNicknames[this.getContactKey(c)] || c.nickname || '',
          selected: false
        }));
        this.contactsList = enriched;
        this.filteredContacts = [...this.contactsList];
        this.splitwiseService.setCachedContacts(userId, contacts);
      },
      error: (err) => {
        console.warn('Using cached contacts only', err);
      }
    });
  }

  getContactKey(contact: SplitContact): string {
    return (contact.phoneNumber || contact.name).trim().toLowerCase();
  }

  getContactDisplayName(contact: SplitContact): string {
    const key = this.getContactKey(contact);
    if (this.userNicknames[key]) {
      return this.userNicknames[key];
    }
    return contact.nickname || contact.name;
  }

  onContactSearch(): void {
    const q = this.contactSearchQuery.trim().toLowerCase();
    if (!q) {
      this.filteredContacts = [...this.contactsList];
      return;
    }

    // Filter local cached contacts instantly
    this.filteredContacts = this.contactsList.filter(c => {
      const displayName = this.getContactDisplayName(c).toLowerCase();
      const realName = c.name.toLowerCase();
      const phone = (c.phoneNumber || '').toLowerCase();
      const email = (c.email || '').toLowerCase();
      return displayName.includes(q) || realName.includes(q) || phone.includes(q) || email.includes(q);
    });

    // If query has 2+ characters, also search in DB for registered users not yet in contacts
    if (q.length >= 2) {
      this.isSearchingContacts = true;
      this.splitwiseService.searchUsers(q).subscribe({
        next: (dbUsers) => {
          this.isSearchingContacts = false;
          for (const u of dbUsers) {
            const exists = this.contactsList.some(c =>
              c.name.toLowerCase() === u.name.toLowerCase() ||
              (c.phoneNumber && u.phoneNumber && c.phoneNumber === u.phoneNumber) ||
              (c.email && u.email && c.email.toLowerCase() === u.email.toLowerCase())
            );
            if (!exists) {
              const enrichedUser: SplitContact = {
                ...u,
                nickname: this.userNicknames[this.getContactKey(u)] || '',
                selected: false
              };
              this.contactsList.push(enrichedUser);
              this.filteredContacts.push(enrichedUser);
            }
          }
          // Cache discovered contacts
          const userId = this.userId || 1;
          this.splitwiseService.setCachedContacts(userId, this.contactsList);
        },
        error: () => {
          this.isSearchingContacts = false;
        }
      });
    }
  }

  toggleContactSelection(contact: SplitContact): void {
    contact.selected = !contact.selected;
  }

  getSelectedContactsCount(): number {
    return this.contactsList.filter(c => c.selected).length;
  }

  submitSelectedContacts(): void {
    const selected = this.contactsList.filter(c => c.selected);
    if (selected.length === 0) {
      this.showContactsModal = false;
      return;
    }

    if (this.contactsSourceContext === 'addGroup') {
      const existingNames = this.memberNamesInput
        ? this.memberNamesInput.split(',').map(s => s.trim()).filter(Boolean)
        : [];

      for (const c of selected) {
        const nameToUse = this.getContactDisplayName(c);
        if (!existingNames.includes(nameToUse)) {
          existingNames.push(nameToUse);
        }
      }

      this.memberNamesInput = existingNames.join(', ');
      this.showContactsModal = false;
      this.showToast(`Added ${selected.length} contact(s) to group members!`, 'success');
    } else if (this.contactsSourceContext === 'addMember') {
      if (selected.length > 0) {
        const first = selected[0];
        this.newMember.name = this.getContactDisplayName(first);
        this.newMember.emailOrPhone = first.phoneNumber || '';
        this.showContactsModal = false;
      }
    } else {
      // Direct / General Contacts view: Launch group creation with selected contacts
      const selectedNames = selected.map(c => this.getContactDisplayName(c));
      this.showContactsModal = false;
      this.openAddGroup();
      this.memberNamesInput = selectedNames.join(', ');
      this.showToast(`Selected ${selected.length} contact(s) for new group!`, 'success');
    }
  }

  openNicknameEditor(contact: SplitContact, event: MouseEvent): void {
    event.stopPropagation();
    this.editingNicknameContact = contact;
    this.nicknameInput = this.userNicknames[this.getContactKey(contact)] || contact.nickname || contact.name;
    this.showEditNicknameModal = true;
  }

  saveNickname(): void {
    if (!this.editingNicknameContact) return;
    const userId = this.userId || 1;
    const key = this.getContactKey(this.editingNicknameContact);
    const newNick = this.nicknameInput.trim();

    this.splitwiseService.setUserNickname(userId, key, newNick);
    this.userNicknames[key] = newNick;
    this.editingNicknameContact.nickname = newNick;

    this.showEditNicknameModal = false;
    this.editingNicknameContact = null;
    this.showToast(`Custom nickname saved for your contacts!`, 'success');
  }

  clearNickname(): void {
    if (!this.editingNicknameContact) return;
    const userId = this.userId || 1;
    const key = this.getContactKey(this.editingNicknameContact);

    this.splitwiseService.setUserNickname(userId, key, '');
    delete this.userNicknames[key];
    this.editingNicknameContact.nickname = '';

    this.showEditNicknameModal = false;
    this.editingNicknameContact = null;
    this.showToast(`Reset to original name.`, 'info');
  }
}
