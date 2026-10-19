import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AiService } from '../services/ai.service';
import { CategoryService } from '../services/category.service';
import { TransactionService } from '../services/transaction.service';
import { AuthService } from '../services/auth.service';
import { DialogService } from '../services/dialog.service';
import { Category } from '../models/category.model';
import { AiBillScanResponse } from '../models/ai-scan.model';
import { TransactionRequest } from '../models/transaction.model';

export interface ReceiptPhoto {
  file: File;
  previewUrl: string;
  name: string;
  sizeFormatted: string;
}

@Component({
  selector: 'app-ai-scanner',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './ai-scanner.component.html',
  styleUrls: ['./ai-scanner.component.css']
})
export class AiScannerComponent implements OnInit {
  activeTab: 'image' | 'text' = 'image';

  // Multi-Photo Receipt Upload State
  selectedPhotos: ReceiptPhoto[] = [];
  isDragging: boolean = false;

  // Text Statement State
  statementText: string = '';

  // AI Processing State
  scanning: boolean = false;
  scanProgressText: string = 'Analyzing with Gemini AI...';
  scanSuccess: boolean = false;
  aiResponse: AiBillScanResponse | null = null;
  errorMessage: string = '';
  successMessage: string = '';
  isQuotaError: boolean = false;

  // Gemini API Key Management Modal
  showKeyModal: boolean = false;
  apiKeyInput: string = '';
  showKeyText: boolean = false;
  testingKey: boolean = false;
  savingKey: boolean = false;
  testStatus: { success: boolean; message: string } | null = null;
  keyStatus: { configured: boolean; maskedKey: string } | null = null;
  hasCustomKey: boolean = false;

  // Review & Edit Form
  reviewForm: FormGroup;
  categories: Category[] = [];
  filteredCategories: Category[] = [];
  saving: boolean = false;

  constructor(
    private fb: FormBuilder,
    private aiService: AiService,
    private categoryService: CategoryService,
    private transactionService: TransactionService,
    private authService: AuthService,
    private router: Router,
    private dialogService: DialogService
  ) {
    const today = new Date().toISOString().substring(0, 10);

    this.reviewForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      amount: ['', [Validators.required, Validators.min(0.01), Validators.max(100000000)]],
      type: ['EXPENSE', [Validators.required]],
      categoryId: ['', [Validators.required]],
      transactionDate: [today, [Validators.required]],
      description: ['', [Validators.maxLength(500)]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.loadKeyStatus();
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

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.filterCategoriesByType(this.reviewForm.get('type')?.value || 'EXPENSE');
      },
      error: () => {
        this.errorMessage = 'Failed to load categories.';
      }
    });
  }

  filterCategoriesByType(type: string): void {
    this.filteredCategories = this.categories.filter((c) => c.type === type);
  }

  onTypeChange(): void {
    const selectedType = this.reviewForm.get('type')?.value;
    this.filterCategoriesByType(selectedType);
    if (this.filteredCategories.length > 0) {
      this.reviewForm.patchValue({ categoryId: this.filteredCategories[0].id });
    }
  }

  // Multi-File Handling
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.handleFiles(Array.from(input.files));
      input.value = '';
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      this.handleFiles(Array.from(event.dataTransfer.files));
    }
  }

  handleFiles(files: File[]): void {
    const validImageFiles = files.filter((f) => f.type.startsWith('image/'));
    if (validImageFiles.length === 0) {
      this.errorMessage = 'Please upload valid image files (PNG, JPG, JPEG, WEBP).';
      return;
    }
    this.errorMessage = '';
    this.isQuotaError = false;

    for (const file of validImageFiles) {
      // Check for duplicate file
      const exists = this.selectedPhotos.some(
        (p) => p.name === file.name && p.file.size === file.size
      );
      if (exists) continue;

      const reader = new FileReader();
      reader.onload = () => {
        this.selectedPhotos.push({
          file,
          previewUrl: reader.result as string,
          name: file.name,
          sizeFormatted: this.formatFileSize(file.size)
        });
      };
      reader.readAsDataURL(file);
    }
  }

  removePhoto(index: number): void {
    this.selectedPhotos.splice(index, 1);
    if (this.selectedPhotos.length === 0) {
      this.errorMessage = '';
      this.isQuotaError = false;
    }
  }

  clearAllPhotos(): void {
    this.selectedPhotos = [];
    this.scanSuccess = false;
    this.aiResponse = null;
    this.errorMessage = '';
    this.isQuotaError = false;
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  // Quick sample text selection
  setSampleStatement(sample: string): void {
    this.statementText = sample;
  }

  // AI Trigger
  startScan(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.isQuotaError = false;

    if (this.activeTab === 'image') {
      if (this.selectedPhotos.length === 0) {
        this.errorMessage = 'Please take or upload at least one receipt photo first.';
        return;
      }
      this.scanImages();
    } else {
      if (!this.statementText.trim()) {
        this.errorMessage = 'Please enter or paste bill statement text.';
        return;
      }
      this.scanText();
    }
  }

  private scanImages(): void {
    if (this.selectedPhotos.length === 0) return;

    this.scanning = true;
    const photoCount = this.selectedPhotos.length;
    this.scanProgressText = photoCount > 1
      ? `Analyzing ${photoCount} receipt photos with Gemini AI...`
      : 'Scanning receipt image with Gemini Vision AI...';

    const rawFiles = this.selectedPhotos.map((p) => p.file);
    this.aiService.scanReceiptImages(rawFiles).subscribe({
      next: (response) => {
        this.scanning = false;
        this.handleAiSuccess(response);
      },
      error: (err) => {
        this.scanning = false;
        const msg = err.error?.message || err.message || '';
        this.checkIfQuotaError(msg);
        this.errorMessage = msg || 'Gemini AI was unable to scan the receipt images. Check your photos or update your API key.';
      }
    });
  }

  private scanText(): void {
    this.scanning = true;
    this.scanProgressText = 'Analyzing bill statement text with Gemini AI...';

    this.aiService.scanBillStatement(this.statementText).subscribe({
      next: (response) => {
        this.scanning = false;
        this.handleAiSuccess(response);
      },
      error: (err) => {
        this.scanning = false;
        const msg = err.error?.message || err.message || '';
        this.checkIfQuotaError(msg);
        this.errorMessage = msg || 'Gemini AI was unable to analyze this statement. Please try again.';
      }
    });
  }

  private checkIfQuotaError(msg: string): void {
    const lower = (msg || '').toLowerCase();
    if (
      lower.includes('quota') ||
      lower.includes('429') ||
      lower.includes('rate limit') ||
      lower.includes('resource_exhausted') ||
      lower.includes('api_key') ||
      lower.includes('key_invalid')
    ) {
      this.isQuotaError = true;
    }
  }

  private handleAiSuccess(response: AiBillScanResponse): void {
    this.aiResponse = response;
    this.scanSuccess = true;
    this.isQuotaError = false;

    // Filter categories by the returned type
    this.filterCategoriesByType(response.type);

    // Populate the editable review form
    this.reviewForm.patchValue({
      title: response.title || 'Bill Payment',
      amount: response.amount || '',
      type: response.type || 'EXPENSE',
      categoryId: response.categoryId || (this.filteredCategories.length > 0 ? this.filteredCategories[0].id : ''),
      transactionDate: response.transactionDate || new Date().toISOString().substring(0, 10),
      description: response.description || ''
    });
  }

  // Key Modal Management within Scanner
  openKeyModal(): void {
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
            message: `Key verified! Connected to Gemini (${res.model || 'gemini-1.5-flash'}).`
          };
        } else {
          this.testStatus = { success: false, message: res.message || 'Key test failed.' };
        }
      },
      error: (err) => {
        this.testingKey = false;
        this.testStatus = {
          success: false,
          message: err.error?.message || 'Failed to verify key with Gemini API.'
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
        this.testStatus = { success: true, message: 'Gemini API Key activated for all scans!' };
        this.loadKeyStatus();
        this.errorMessage = '';
        this.isQuotaError = false;
        setTimeout(() => this.closeKeyModal(), 1200);
      },
      error: () => {
        this.savingKey = false;
        this.testStatus = { success: true, message: 'Key saved locally in your browser/device!' };
        this.loadKeyStatus();
        this.errorMessage = '';
        this.isQuotaError = false;
        setTimeout(() => this.closeKeyModal(), 1200);
      }
    });
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

  // Submit Reviewed & Edited Transaction to Database
  onSubmitTransaction(): void {
    if (this.reviewForm.invalid) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    const user = this.authService.getCurrentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const formVal = this.reviewForm.value;
    const requestData: TransactionRequest = {
      title: formVal.title.trim(),
      amount: Number(formVal.amount),
      type: formVal.type,
      categoryId: Number(formVal.categoryId),
      transactionDate: formVal.transactionDate,
      description: formVal.description ? formVal.description.trim() : '',
      userId: user.id
    };

    this.transactionService.createTransaction(requestData).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Transaction successfully added to your records!';
        setTimeout(() => {
          this.router.navigate(['/transactions']);
        }, 1200);
      },
      error: (err) => {
        this.saving = false;
        const msg = err.error?.message || '';
        if (msg.includes('User not found')) {
          this.errorMessage = 'Your session has expired. Please log in again to save transactions.';
        } else {
          this.errorMessage = msg || 'Failed to save transaction to database.';
        }
      }
    });
  }

  resetScan(): void {
    this.scanSuccess = false;
    this.aiResponse = null;
    this.clearAllPhotos();
    this.statementText = '';
    this.errorMessage = '';
    this.successMessage = '';
    this.isQuotaError = false;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.reviewForm.get(fieldName);
    return !!(field && field.touched && field.invalid);
  }
}

