package com.ecommerce.product.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "category_id"}))
public class ProductCategory {
    // ID embebido para clave compuesta (product_id, category_id)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    //  Relación con producto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // <— este es el lado dueño en la relación inversa
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ProductCategory() {
    }

    public ProductCategory(Product product, UUID categoryId) {
        this.product = product;
        this.categoryId = categoryId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /* Se ejecuta antes de guardar */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
