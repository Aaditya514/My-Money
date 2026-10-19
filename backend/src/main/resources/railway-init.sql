-- ============================================================
-- Finance Tracker - Railway MySQL Database Initialization
-- Run this single SQL script in Railway MySQL to create
-- all tables and seed initial data.
-- ============================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME(6)
);

-- 2. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL
);

-- 3. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(255) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    budget_month INT NOT NULL,
    budget_year INT NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Seed Default Categories (only if the table is empty)
INSERT INTO categories (name, type)
SELECT name, type FROM (
    SELECT 'Salary' AS name, 'INCOME' AS type UNION ALL
    SELECT 'Freelance', 'INCOME' UNION ALL
    SELECT 'Investments', 'INCOME' UNION ALL
    SELECT 'Food', 'EXPENSE' UNION ALL
    SELECT 'Transport', 'EXPENSE' UNION ALL
    SELECT 'Shopping', 'EXPENSE' UNION ALL
    SELECT 'Bills', 'EXPENSE' UNION ALL
    SELECT 'Entertainment', 'EXPENSE' UNION ALL
    SELECT 'Education', 'EXPENSE' UNION ALL
    SELECT 'Healthcare', 'EXPENSE' UNION ALL
    SELECT 'Other', 'EXPENSE'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories LIMIT 1);

