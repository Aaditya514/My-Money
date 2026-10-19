package com.financetracker.repository;

import com.financetracker.entity.Category;
import com.financetracker.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(CategoryType type);

    Optional<Category> findByName(String name);

    Optional<Category> findByNameAndType(String name, CategoryType type);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND (:includeArchived = true OR c.isArchived = false) ORDER BY c.orderIndex ASC, c.name ASC")
    List<Category> findAvailableCategories(@Param("userId") Long userId, @Param("includeArchived") boolean includeArchived);

    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND c.type = :type AND (:includeArchived = true OR c.isArchived = false) ORDER BY c.orderIndex ASC, c.name ASC")
    List<Category> findAvailableCategoriesByType(@Param("userId") Long userId, @Param("type") CategoryType type, @Param("includeArchived") boolean includeArchived);
}
