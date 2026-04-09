package com.ecommerce.product.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private double price;
    private String sku;
    private Integer stock;
    private Boolean status;
    private String description;
    private String image;
    /*
    * Relación con la entidad pivote. No exponemos directamente Category aquí para contener el control
    * sobre la asociación y permitir añadir metadatos en la pivote.
    * Relación de muchos a muchos a travéz de la tabla pivote.
    * */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductCategory> categories = new HashSet<>();
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product() {
    }

    public Product(UUID id, String name, double price, String sku, Integer stock, Boolean status,
                   String description, String image, Set<ProductCategory> categories, LocalDateTime createdAt,
                   LocalDateTime updatedAt, LocalDateTime deletedAt)
    {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sku = sku;
        this.stock = stock;
        this.status = status;
        this.description = description;
        this.image = image;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<ProductCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<ProductCategory> categories) {
        this.categories = categories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    //  Se ejecuta antes de guardar el registro en la DB
    @PrePersist
    void onCreated() {
        this.status = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    //  Se ejecuta antes de actualizar en la DB
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addCategory(UUID categoryId) {
        ProductCategory pc = new ProductCategory();
        pc.setProduct(this);
        pc.setCategoryId(categoryId);

        this.categories.add(pc);
    }

    public void removeCategory(UUID categoryId) {
        this.categories.removeIf(pc -> pc.getCategoryId().equals(categoryId));
    }
}
