import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryService } from '../services/category.service';
import { AuthService } from '../services/auth.service';
import { DialogService } from '../services/dialog.service';
import { Category, CategoryType } from '../models/category.model';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  categoryForm: FormGroup;
  editingCategoryId: number | null = null;

  showArchived: boolean = false;
  loading: boolean = true;
  saving: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  readonly categoryTypes: { value: CategoryType; label: string; desc: string }[] = [
    { value: 'EXPENSE', label: 'Expense', desc: 'Day-to-day bills, shopping, food' },
    { value: 'INCOME', label: 'Income', desc: 'Salary, freelance, dividends' },
    { value: 'SAVINGS', label: 'Savings Allocation', desc: 'Emergency reserve, sinking funds' },
    { value: 'OTHER', label: 'Other Planning Item', desc: 'Custom discretionary or misc targets' }
  ];

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private authService: AuthService,
    private dialogService: DialogService
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z0-9\s&\/\-']+$/)
      ]],
      type: ['EXPENSE', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.categoryForm.get(fieldName);
    return !!(field && field.touched && field.invalid);
  }

  loadCategories(): void {
    const userId = this.authService.getUserId();
    this.loading = true;
    this.categoryService.getCategories(undefined, userId, this.showArchived).subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not load categories.';
      }
    });
  }

  toggleShowArchived(): void {
    this.showArchived = !this.showArchived;
    this.loadCategories();
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const userId = this.authService.getUserId();
    this.saving = true;
    this.errorMessage = '';
    const formVal = this.categoryForm.value;
    const requestData = {
      name: formVal.name.trim(),
      type: formVal.type as CategoryType,
      userId: userId
    };

    if (this.editingCategoryId) {
      this.categoryService.updateCategory(this.editingCategoryId, requestData).subscribe({
        next: () => {
          this.saving = false;
          this.successMessage = 'Category updated successfully!';
          this.cancelEdit();
          this.loadCategories();
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to update category.';
        }
      });
    } else {
      this.categoryService.createCategory(requestData).subscribe({
        next: () => {
          this.saving = false;
          this.successMessage = 'Category added successfully!';
          this.categoryForm.reset({ type: 'EXPENSE' });
          this.loadCategories();
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.message || 'Failed to add category.';
        }
      });
    }
  }

  onEdit(category: Category): void {
    this.editingCategoryId = category.id;
    this.categoryForm.patchValue({
      name: category.name,
      type: category.type
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelEdit(): void {
    this.editingCategoryId = null;
    this.categoryForm.reset({ type: 'EXPENSE' });
  }

  toggleArchive(category: Category): void {
    const newState = !category.isArchived;
    this.categoryService.archiveCategory(category.id, newState).subscribe({
      next: () => {
        this.successMessage = `Category '${category.name}' ${newState ? 'archived' : 'unarchived'}.`;
        this.loadCategories();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: () => {
        this.errorMessage = 'Failed to update archive status.';
      }
    });
  }

  moveUp(index: number): void {
    if (index <= 0) return;
    const temp = this.categories[index];
    this.categories[index] = this.categories[index - 1];
    this.categories[index - 1] = temp;
    this.saveOrder();
  }

  moveDown(index: number): void {
    if (index >= this.categories.length - 1) return;
    const temp = this.categories[index];
    this.categories[index] = this.categories[index + 1];
    this.categories[index + 1] = temp;
    this.saveOrder();
  }

  saveOrder(): void {
    const ids = this.categories.map((c) => c.id);
    this.categoryService.reorderCategories(ids).subscribe({
      next: () => {},
      error: () => {}
    });
  }

  async onDelete(category: Category): Promise<void> {
    const confirmed = await this.dialogService.confirm({
      title: 'Delete Category',
      message: `Are you sure you want to delete '${category.name}'? Historical transactions will retain their record, but this category won't be selectable for new entries.`,
      confirmText: 'Delete Category',
      cancelText: 'Keep Category',
      type: 'danger',
      icon: 'trash'
    });

    if (confirmed) {
      this.categoryService.deleteCategory(category.id).subscribe({
        next: () => {
          this.successMessage = `Category '${category.name}' deleted successfully.`;
          this.loadCategories();
          setTimeout(() => (this.successMessage = ''), 3000);
        },
        error: () => {
          this.errorMessage = 'Failed to delete category.';
        }
      });
    }
  }
}
