package com.ecommerce.product.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_categories")
public class ProductCategory {
    // ID embebido para clave compuesta (product_id, category_id)
    @EmbeddedId
    private ProductCategoryId id;

    //  Relación con producto
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId") // Vincula parte de la clave compuesta con la entidad Product
    @JoinColumn(name = "product_id")
    private Product product; // <— este es el lado dueño en la relación inversa

    // Solo guardamos el ID de la categoría, ya que la entidad "Category" vive en otro microservicio
    // El campo 'categoryId' ya es parte de la clave compuesta (ProductCategoryId)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ProductCategory() {
    }

    public ProductCategory(Product product, Long categoryId) {
        this.id = new ProductCategoryId(product.getId(), categoryId);
        this.product = product;
    }

    public ProductCategoryId getId() {
        return id;
    }

    public void setId(ProductCategoryId id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
        //this.updatedAt = LocalDateTime.now();
    }

    /* Se ejecuta antes de actualizar */
    @PreUpdate
    void onUpdate() {
        //this.updatedAt = LocalDateTime.now();
    }
}
