package com.financetracker.service;

import com.financetracker.dto.CategoryRequest;
import com.financetracker.dto.CategoryResponse;
import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import com.financetracker.entity.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return getAllCategories(null, false);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long userId, Boolean includeArchived) {
        boolean showArchived = includeArchived != null && includeArchived;
        List<Category> categories;
        if (userId != null) {
            categories = categoryRepository.findAvailableCategories(userId, showArchived);
        } else {
            categories = categoryRepository.findAll();
            if (!showArchived) {
                categories = categories.stream().filter(c -> !c.isArchived()).toList();
            }
        }

        List<CategoryResponse> responseList = new ArrayList<>();
        for (Category category : categories) {
            responseList.add(mapToResponse(category));
        }
        return responseList;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(CategoryType type) {
        return getCategoriesByType(type, null, false);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByType(CategoryType type, Long userId, Boolean includeArchived) {
        boolean showArchived = includeArchived != null && includeArchived;
        List<Category> categories;
        if (userId != null) {
            categories = categoryRepository.findAvailableCategoriesByType(userId, type, showArchived);
        } else {
            categories = categoryRepository.findByType(type);
            if (!showArchived) {
                categories = categories.stream().filter(c -> !c.isArchived()).toList();
            }
        }

        List<CategoryResponse> responseList = new ArrayList<>();
        for (Category category : categories) {
            responseList.add(mapToResponse(category));
        }
        return responseList;
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        Category category = new Category(
                request.getName().trim(),
                request.getType(),
                request.getIsArchived() != null ? request.getIsArchived() : false,
                request.getOrderIndex() != null ? request.getOrderIndex() : 0,
                user
        );
        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getCategoryEntityById(id);
        category.setName(request.getName().trim());
        category.setType(request.getType());
        if (request.getIsArchived() != null) {
            category.setArchived(request.getIsArchived());
        }
        if (request.getOrderIndex() != null) {
            category.setOrderIndex(request.getOrderIndex());
        }
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public CategoryResponse archiveCategory(Long id, boolean archived) {
        Category category = getCategoryEntityById(id);
        category.setArchived(archived);
        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public void reorderCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;
        for (int i = 0; i < categoryIds.size(); i++) {
            Long catId = categoryIds.get(i);
            Category cat = categoryRepository.findById(catId).orElse(null);
            if (cat != null) {
                cat.setOrderIndex(i);
                categoryRepository.save(cat);
            }
        }
    }

    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id);

        // Before deleting, detach from transactions so historical records are preserved without constraint violation
        transactionRepository.findByCategoryId(id).forEach(t -> {
            t.setCategory(null);
            transactionRepository.save(t);
        });

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category c) {
        Long uId = c.getUser() != null ? c.getUser().getId() : null;
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getType(),
                c.isArchived(),
                c.getOrderIndex(),
                uId
        );
    }
}
