-- V1__init_schema.sql
-- Initial database schema for Fintel production deployment

-- 1. Users
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    CONSTRAINT `uk_users_email` UNIQUE (`email`)
);

-- 2. Transaction Categories
CREATE TABLE IF NOT EXISTS `transaction_category` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `category_name` VARCHAR(100) NOT NULL,
    `category_color` VARCHAR(255) NOT NULL,
    CONSTRAINT `fk_category_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- 3. Transactions
CREATE TABLE IF NOT EXISTS `transaction` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT NULL,
    `user_id` INT NOT NULL,
    `transaction_name` VARCHAR(255) NOT NULL,
    `transaction_amount` DOUBLE NOT NULL,
    `transaction_date` DATE NOT NULL,
    `transaction_time` VARCHAR(255) NULL,
    `transaction_type` VARCHAR(255) NOT NULL,
    CONSTRAINT `fk_transaction_category` FOREIGN KEY (`category_id`) REFERENCES `transaction_category` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- Composite indexes for transaction query performance
CREATE INDEX `idx_transaction_user_date` ON `transaction` (`user_id`, `transaction_date`);
CREATE INDEX `idx_transaction_user_category` ON `transaction` (`user_id`, `category_id`);

-- 4. Budgets
CREATE TABLE IF NOT EXISTS `budgets` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `category` VARCHAR(120) NOT NULL,
    `limit_amount` DECIMAL(15, 2) NOT NULL,
    `spent_amount` DECIMAL(15, 2) NULL DEFAULT 0.00,
    `year` INT NOT NULL,
    `period_type` VARCHAR(20) NOT NULL,
    `month` INT NULL,
    `quarter` INT NULL,
    CONSTRAINT `fk_budget_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- Index for budget category lookup
CREATE INDEX `idx_budget_user_category` ON `budgets` (`user_id`, `category`);

-- 5. Savings Goals
CREATE TABLE IF NOT EXISTS `savings_goals` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `name` VARCHAR(90) NOT NULL,
    `target_amount` DECIMAL(15, 2) NOT NULL,
    `current_amount` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `deadline` DATE NULL,
    `completed` BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT `fk_goal_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- 6. Password Reset Tokens
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `token` VARCHAR(255) NOT NULL,
    `user_id` INT NOT NULL,
    `expiry_date` DATETIME(6) NOT NULL,
    `used` BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT `uk_prt_token` UNIQUE (`token`),
    CONSTRAINT `fk_prt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- 7. Refresh Tokens
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `token_hash` VARCHAR(64) NOT NULL,
    `user_id` INT NOT NULL,
    `expires_at` TIMESTAMP(6) NOT NULL,
    `revoked` BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT `fk_rt_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

-- Indexes for refresh tokens
CREATE INDEX `idx_refresh_token_hash` ON `refresh_tokens` (`token_hash`);
CREATE INDEX `idx_refresh_token_user` ON `refresh_tokens` (`user_id`);
