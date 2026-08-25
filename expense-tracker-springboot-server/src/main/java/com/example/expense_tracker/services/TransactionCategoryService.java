package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.TransactionCategoryRepository;
import com.example.expense_tracker.repositories.TransactionRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionCategoryService {
    private static final Logger log = LoggerFactory.getLogger(TransactionCategoryService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Optional<TransactionCategory> getTransactionCategoryById(int id) {
        log.info("Getting transaction category by id: {}", id);
        return transactionCategoryRepository.findById(id);
    }

    public List<TransactionCategory> getAllTransactionCategoriesByUserId(int userId) {
        log.info("Getting all transaction categories for user: {}", userId);
        return transactionCategoryRepository.findAllByUserId(userId);
    }

    @Transactional
    public TransactionCategory createTransactionCategory(int userId, String categoryName, String categoryColor) {
        log.info("Creating category: '{}' for user: {}", categoryName, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        TransactionCategory transactionCategory = new TransactionCategory();
        transactionCategory.setUser(user);
        transactionCategory.setCategoryName(categoryName);
        transactionCategory.setCategoryColor(categoryColor);

        return transactionCategoryRepository.save(transactionCategory);
    }

    @Transactional
    public TransactionCategory updateTransactionCategoryById(int transactionCategoryId, String newCategoryName,
                                                             String newCategoryColor) {
        log.info("Updating TransactionCategory with Id: {}", transactionCategoryId);

        TransactionCategory category = transactionCategoryRepository.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + transactionCategoryId));

        category.setCategoryName(newCategoryName);
        category.setCategoryColor(newCategoryColor);
        return transactionCategoryRepository.save(category);
    }

    @Transactional
    public boolean deleteTransactionCategoryById(int transactionCategoryId) {
        log.info("Deleting transaction category with id: {}", transactionCategoryId);

        Optional<TransactionCategory> categoryOpt = transactionCategoryRepository.findById(transactionCategoryId);
        if (categoryOpt.isEmpty()) {
            log.warn("Attempted to delete non-existent category: {}", transactionCategoryId);
            return false;
        }

        int unlinkedCount = transactionRepository.unlinkCategory(transactionCategoryId);
        log.info("Unlinked {} transactions from category {}", unlinkedCount, transactionCategoryId);

        transactionCategoryRepository.delete(categoryOpt.get());
        log.info("Successfully deleted category: {}", transactionCategoryId);
        return true;
    }
}