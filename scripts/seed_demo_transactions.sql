SET @UID = 3;

-- Make sure accounts exist
INSERT IGNORE INTO accounts (user_id, name, account_type, balance, currency, is_default) VALUES
(@UID, 'HDFC Salary Account', 'CHECKING', 185000.00, 'INR', 1),
(@UID, 'SBI Savings Account', 'SAVINGS', 95000.00, 'INR', 0),
(@UID, 'ICICI Coral Credit Card', 'CREDIT_CARD', -8500.00, 'INR', 0),
(@UID, 'Cash Wallet', 'CASH', 7500.00, 'INR', 0);

SET @ACC_HDFC = (SELECT id FROM accounts WHERE user_id = @UID AND name = 'HDFC Salary Account' LIMIT 1);
SET @ACC_SBI  = (SELECT id FROM accounts WHERE user_id = @UID AND name = 'SBI Savings Account' LIMIT 1);
SET @ACC_CARD = (SELECT id FROM accounts WHERE user_id = @UID AND name = 'ICICI Coral Credit Card' LIMIT 1);
SET @ACC_CASH = (SELECT id FROM accounts WHERE user_id = @UID AND name = 'Cash Wallet' LIMIT 1);

-- Get Category IDs
SET @CAT_SALARY     = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Salary' LIMIT 1);
SET @CAT_FREELANCE  = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Freelance' LIMIT 1);
SET @CAT_GROCERIES  = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Groceries & Food' LIMIT 1);
SET @CAT_DINING     = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Dining & Cafe' LIMIT 1);
SET @CAT_RENT       = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Housing & Rent' LIMIT 1);
SET @CAT_UTILITIES  = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Utilities & Bills' LIMIT 1);
SET @CAT_TRANSPORT  = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Transportation & Fuel' LIMIT 1);
SET @CAT_SHOPPING   = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Shopping & Gadgets' LIMIT 1);
SET @CAT_ENTERTAIN  = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Entertainment & Subscriptions' LIMIT 1);
SET @CAT_HEALTH     = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Healthcare & Fitness' LIMIT 1);
SET @CAT_INVEST     = (SELECT id FROM transaction_category WHERE user_id = @UID AND category_name = 'Investment & Savings' LIMIT 1);

-- Clean old 2026 data for clean state
DELETE FROM `transaction` WHERE user_id = @UID AND YEAR(transaction_date) = 2026;

-- Insert Jan - Dec 2026 Transactions
INSERT INTO `transaction` (user_id, account_id, category_id, transaction_name, transaction_amount, transaction_type, transaction_date, transaction_time) VALUES
-- JANUARY 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-01-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-01-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Supermarket Groceries', 11500.00, 'EXPENSE', '2026-01-05', '17:30:00'),
(@UID, @ACC_CARD, @CAT_UTILITIES, 'Broadband & Power Bill', 3200.00, 'EXPENSE', '2026-01-08', '14:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Fuel & Metro Card', 4500.00, 'EXPENSE', '2026-01-12', '09:15:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Team Lunch & Cafe', 3800.00, 'EXPENSE', '2026-01-18', '13:30:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'Web Consulting Project', 30000.00, 'INCOME', '2026-01-22', '18:00:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Winter Apparel & Shoes', 6800.00, 'EXPENSE', '2026-01-25', '16:45:00'),

-- FEBRUARY 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-02-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-02-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Fresh Mart Provisions', 10200.00, 'EXPENSE', '2026-02-06', '18:00:00'),
(@UID, @ACC_CARD, @CAT_UTILITIES, 'Electricity & Gas Bill', 2900.00, 'EXPENSE', '2026-02-10', '12:30:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Cab Rides & Fuel', 4200.00, 'EXPENSE', '2026-02-14', '15:20:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Fine Dining & Snacks', 4900.00, 'EXPENSE', '2026-02-19', '20:15:00'),
(@UID, @ACC_CARD, @CAT_ENTERTAIN, 'Movie Tickets & Streaming', 1800.00, 'EXPENSE', '2026-02-23', '19:00:00'),

-- MARCH 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-03-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-03-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'BigBasket Order', 12800.00, 'EXPENSE', '2026-03-05', '17:45:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'UI Design Sprint', 35000.00, 'INCOME', '2026-03-10', '16:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Monthly Fuel', 5100.00, 'EXPENSE', '2026-03-15', '09:30:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Weekend Dining Out', 5200.00, 'EXPENSE', '2026-03-20', '21:00:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Tech Accessories & Cable', 4500.00, 'EXPENSE', '2026-03-24', '14:10:00'),

-- APRIL 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-04-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-04-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Monthly Grocery Mart', 11000.00, 'EXPENSE', '2026-04-06', '16:00:00'),
(@UID, @ACC_CARD, @CAT_UTILITIES, 'Water & Electricity', 3400.00, 'EXPENSE', '2026-04-09', '11:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Car Petrol & Fastag', 4800.00, 'EXPENSE', '2026-04-14', '08:45:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Cafe Coffee & Meals', 3900.00, 'EXPENSE', '2026-04-18', '19:30:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Summer Wear Shopping', 8200.00, 'EXPENSE', '2026-04-25', '17:00:00'),

-- MAY 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-05-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-05-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Organic Farm Groceries', 13400.00, 'EXPENSE', '2026-05-05', '18:15:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'Backend Architecture Contract', 45000.00, 'INCOME', '2026-05-09', '15:00:00'),
(@UID, @ACC_CARD, @CAT_UTILITIES, 'Air Conditioner Power Bill', 4800.00, 'EXPENSE', '2026-05-12', '13:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Fuel & Toll Charges', 5300.00, 'EXPENSE', '2026-05-16', '10:00:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Restaurant Dinners', 5800.00, 'EXPENSE', '2026-05-22', '20:30:00'),

-- JUNE 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-06-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-06-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Supermarket Supplies', 11900.00, 'EXPENSE', '2026-06-06', '17:00:00'),
(@UID, @ACC_CARD, @CAT_UTILITIES, 'Broadband & Phone Plans', 3100.00, 'EXPENSE', '2026-06-10', '12:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Commute & Fuel', 4600.00, 'EXPENSE', '2026-06-15', '09:00:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Casual Dinners & Coffee', 4200.00, 'EXPENSE', '2026-06-20', '18:30:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Gadgets & Home Improvement', 9500.00, 'EXPENSE', '2026-06-26', '16:00:00'),

-- JULY 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-07-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'House Rent Payment', 25000.00, 'EXPENSE', '2026-07-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Monthly Kitchen Stock', 12500.00, 'EXPENSE', '2026-07-05', '18:00:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'Mobile App Prototype', 28000.00, 'INCOME', '2026-07-11', '14:00:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Fuel & Auto Expense', 4900.00, 'EXPENSE', '2026-07-16', '10:30:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Barbeque Nation Dinner', 4600.00, 'EXPENSE', '2026-07-21', '20:00:00'),
(@UID, @ACC_CARD, @CAT_ENTERTAIN, 'Concert Tickets & Movies', 3200.00, 'EXPENSE', '2026-07-28', '19:15:00'),

-- AUGUST 2026
(@UID, @ACC_HDFC, @CAT_SALARY, 'August Salary Credit', 125000.00, 'INCOME', '2026-08-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'August House Rent', 25000.00, 'EXPENSE', '2026-08-02', '11:00:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'BigBasket Monthly Grocery', 12400.00, 'EXPENSE', '2026-08-05', '16:30:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Sony WH-1000XM5 Headphones', 24990.00, 'EXPENSE', '2026-08-12', '14:20:00'),
(@UID, @ACC_SBI, @CAT_INVEST, 'Mutual Funds SIP Growth Fund', 15000.00, 'EXPENSE', '2026-08-15', '09:00:00'),
(@UID, @ACC_HDFC, @CAT_TRANSPORT, 'Car Service & Fuel', 8500.00, 'EXPENSE', '2026-08-20', '15:45:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'Figma Prototype Consultation', 20000.00, 'INCOME', '2026-08-25', '18:00:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Weekend Dining Out', 5200.00, 'EXPENSE', '2026-08-28', '21:00:00'),

-- SEPTEMBER 2026 (Current Active Month)
(@UID, @ACC_HDFC, @CAT_SALARY, 'Monthly Salary Credit', 125000.00, 'INCOME', '2026-09-01', '10:00:00'),
(@UID, @ACC_HDFC, @CAT_RENT, 'Apartment Maintenance & Rent', 25000.00, 'EXPENSE', '2026-09-02', '14:30:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Nature Basket Supermarket', 4250.00, 'EXPENSE', '2026-09-03', '18:20:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Starbucks Coffee & Snacks', 650.00, 'EXPENSE', '2026-09-04', '11:15:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'HP Petrol Pump Fuel', 2500.00, 'EXPENSE', '2026-09-05', '09:40:00'),
(@UID, @ACC_SBI, @CAT_FREELANCE, 'UI/UX Design Milestone Payment', 35000.00, 'INCOME', '2026-09-06', '16:00:00'),
(@UID, @ACC_CARD, @CAT_SHOPPING, 'Keychron Mechanical Keyboard', 7500.00, 'EXPENSE', '2026-09-07', '15:10:00'),
(@UID, @ACC_HDFC, @CAT_UTILITIES, 'Electricity Bill TNEB', 2250.00, 'EXPENSE', '2026-09-08', '12:00:00'),
(@UID, @ACC_CARD, @CAT_DINING, 'Barbeque Nation Dinner', 2850.00, 'EXPENSE', '2026-09-09', '20:45:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Blinkit Grocery Order', 1850.00, 'EXPENSE', '2026-09-10', '19:10:00'),
(@UID, @ACC_CARD, @CAT_ENTERTAIN, 'Movie Tickets IMAX', 1200.00, 'EXPENSE', '2026-09-11', '17:30:00'),
(@UID, @ACC_HDFC, @CAT_GROCERIES, 'Fresh Fruits & Vegetables', 2350.00, 'EXPENSE', '2026-09-12', '10:30:00'),
(@UID, @ACC_CASH, @CAT_DINING, 'Cafe Coffee Day', 700.00, 'EXPENSE', '2026-09-12', '16:45:00'),
(@UID, @ACC_CARD, @CAT_TRANSPORT, 'Uber Rides Chennai', 1600.00, 'EXPENSE', '2026-09-13', '13:15:00'),
(@UID, @ACC_HDFC, @CAT_UTILITIES, 'Airtel Postpaid & Wi-Fi', 1200.00, 'EXPENSE', '2026-09-13', '11:00:00'),
(@UID, @ACC_CARD, @CAT_ENTERTAIN, 'BookMyShow Event', 1000.00, 'EXPENSE', '2026-09-13', '18:00:00');

-- Update Budgets for September 2026
DELETE FROM budgets WHERE user_id = @UID;
INSERT INTO budgets (user_id, category, limit_amount, spent_amount, year, period_type, `month`, quarter, rollover) VALUES
(@UID, 'Groceries & Food', 15000.00, 8450.00, 2026, 'MONTHLY', 9, 3, 1),
(@UID, 'Dining & Cafe', 8000.00, 4200.00, 2026, 'MONTHLY', 9, 3, 0),
(@UID, 'Utilities & Bills', 6000.00, 3450.00, 2026, 'MONTHLY', 9, 3, 1),
(@UID, 'Transportation & Fuel', 7000.00, 4100.00, 2026, 'MONTHLY', 9, 3, 0),
(@UID, 'Shopping & Gadgets', 15000.00, 7500.00, 2026, 'MONTHLY', 9, 3, 0),
(@UID, 'Entertainment & Subscriptions', 5000.00, 2200.00, 2026, 'MONTHLY', 9, 3, 1);

-- Update Savings Goals
DELETE FROM savings_goals WHERE user_id = @UID;
INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline, completed) VALUES
(@UID, 'Emergency Fund', 200000.00, 140000.00, '2026-12-31', 0),
(@UID, 'MacBook Pro M4', 180000.00, 95000.00, '2026-11-30', 0),
(@UID, 'Japan Vacation', 250000.00, 60000.00, '2027-04-15', 0),
(@UID, 'Bike Down Payment', 80000.00, 80000.00, '2026-08-15', 1);
