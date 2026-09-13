-- Migration V3: Normalize budgets period_type to VARCHAR(20) to ensure consistent Hibernate schema validation across MySQL versions and legacy enum columns
ALTER TABLE budgets MODIFY COLUMN period_type VARCHAR(20) NOT NULL;
