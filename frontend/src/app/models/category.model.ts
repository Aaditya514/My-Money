export type CategoryType = 'INCOME' | 'EXPENSE' | 'SAVINGS' | 'OTHER';

export interface Category {
  id: number;
  name: string;
  type: CategoryType;
  isArchived?: boolean;
  orderIndex?: number;
  userId?: number;
}

export interface CategoryRequest {
  name: string;
  type: CategoryType;
  isArchived?: boolean;
  orderIndex?: number;
  userId?: number;
}
