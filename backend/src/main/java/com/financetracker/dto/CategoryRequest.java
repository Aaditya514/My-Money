package com.financetracker.dto;

import com.financetracker.entity.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s&/'-]+$", message = "Category name contains invalid characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;

    private Boolean isArchived = false;

    private Integer orderIndex = 0;

    private Long userId;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, CategoryType type) {
        this.name = name;
        this.type = type;
        this.isArchived = false;
        this.orderIndex = 0;
    }

    public CategoryRequest(String name, CategoryType type, Boolean isArchived, Integer orderIndex, Long userId) {
        this.name = name;
        this.type = type;
        this.isArchived = isArchived != null ? isArchived : false;
        this.orderIndex = orderIndex != null ? orderIndex : 0;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
