package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.SavingsGoalRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SavingsGoalService {
    private static final Logger log = LoggerFactory.getLogger(SavingsGoalService.class);

    private final SavingsGoalRepository goals;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository goals, UserRepository userRepository) {
        this.goals = goals;
        this.userRepository = userRepository;
    }

    public Optional<SavingsGoal> getById(Integer id) {
        log.info("Getting savings goal with id: {}", id);
        return goals.findById(id);
    }

    public List<SavingsGoal> listByUser(Integer userId) {
        log.info("Listing savings goals for user: {}", userId);
        return goals.findByUserId(userId);
    }

    public SavingsGoal create(SavingsGoal g, Integer userId) {
        log.info("Creating savings goal: '{}' for user: {}", g.getName(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        g.setUser(user);
        return goals.save(g);
    }

    public SavingsGoal upsert(SavingsGoal g, Integer userId) {
        log.info("Upserting savings goal: '{}' for user: {}", g.getName(), userId);
        if (g.getId() != null) {
            SavingsGoal existing = goals.findById(g.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + g.getId()));
            existing.setName(g.getName());
            existing.setTargetAmount(g.getTargetAmount());
            existing.setCurrentAmount(g.getCurrentAmount());
            existing.setDeadline(g.getDeadline());
            existing.setCompleted(g.isCompleted());
            if (g.getUser() != null) {
                existing.setUser(g.getUser());
            }
            return goals.save(existing);
        }

        if (g.getUser() == null && userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            g.setUser(user);
        }
        return goals.save(g);
    }

    public void delete(Integer id) {
        log.info("Deleting savings goal with id: {}", id);
        if (!goals.existsById(id)) {
            throw new ResourceNotFoundException("Savings goal not found with id: " + id);
        }
        goals.deleteById(id);
    }
}
