-- V2__phase3_features.sql
-- Schema migration for Phase 3: Multi-Account, Recurring Transactions, and Budget Rollover

-- 1. Accounts table
CREATE TABLE IF NOT EXISTS `accounts` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `account_type` VARCHAR(50) NOT NULL DEFAULT 'CHECKING',
    `balance` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'INR',
    `is_default` BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT `fk_accounts_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

CREATE INDEX `idx_accounts_user` ON `accounts` (`user_id`);

-- 2. Link transaction to account
ALTER TABLE `transaction` ADD COLUMN `account_id` INT NULL;
ALTER TABLE `transaction` ADD CONSTRAINT `fk_transaction_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE SET NULL;
CREATE INDEX `idx_transaction_account` ON `transaction` (`account_id`);

-- 3. Recurring transactions table
CREATE TABLE IF NOT EXISTS `recurring_transactions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `category_id` INT NULL,
    `account_id` INT NULL,
    `name` VARCHAR(255) NOT NULL,
    `amount` DOUBLE NOT NULL,
    `transaction_type` VARCHAR(50) NOT NULL,
    `frequency` VARCHAR(50) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `next_execution_date` DATE NOT NULL,
    `last_execution_date` DATE NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT `fk_recurring_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_recurring_category` FOREIGN KEY (`category_id`) REFERENCES `transaction_category` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_recurring_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`) ON DELETE SET NULL
);

CREATE INDEX `idx_recurring_user_next` ON `recurring_transactions` (`user_id`, `next_execution_date`, `active`);

-- 4. Budget Rollover flag
ALTER TABLE `budgets` ADD COLUMN `rollover` BOOLEAN NOT NULL DEFAULT FALSE;
