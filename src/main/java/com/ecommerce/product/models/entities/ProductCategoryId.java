package com.ecommerce.product.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProductCategoryId implements Serializable {
    @Column(name = "product_id")
    private UUID productId;
    @Column(name = "category_id")
    private UUID categoryId;

    public ProductCategoryId() {
    }

    public ProductCategoryId(UUID productId, UUID categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ProductCategoryId that = (ProductCategoryId) object;
        return Objects.equals(productId, that.productId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, categoryId);
    }
}
