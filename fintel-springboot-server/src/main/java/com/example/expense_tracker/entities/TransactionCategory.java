package com.example.expense_tracker.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "transaction_category")
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @NotBlank(message = "Category color cannot be blank")
    @Column(name = "category_color", nullable = false)
    private String categoryColor;

    public TransactionCategory() {}

    public TransactionCategory(Integer id, User user, String categoryName, String categoryColor) {
        this.id = id;
        this.user = user;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }
}
