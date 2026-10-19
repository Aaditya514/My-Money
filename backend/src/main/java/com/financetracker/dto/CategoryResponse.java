package com.financetracker.dto;

import com.financetracker.entity.CategoryType;

public class CategoryResponse {

    private Long id;
    private String name;
    private CategoryType type;
    private boolean isArchived;
    private int orderIndex;
    private Long userId;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, CategoryType type) {
        this(id, name, type, false, 0, null);
    }

    public CategoryResponse(Long id, String name, CategoryType type, boolean isArchived, int orderIndex, Long userId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isArchived = isArchived;
        this.orderIndex = orderIndex;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
