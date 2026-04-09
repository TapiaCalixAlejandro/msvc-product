package com.ecommerce.product.models.dtos;

import java.util.Set;
import java.util.UUID;

public class ProductResponse {
    private UUID id;
    private String name;
    private Double price;
    private String sku;
    private Integer stock;
    private String description;
    private Boolean status;
    private String image;
    private Set<CategoryResponse> categories;

    public ProductResponse() {
    }

    public ProductResponse(UUID id, String name, Double price, String sku, Integer stock, String description, Boolean status, String image, Set<CategoryResponse> categories) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.sku = sku;
        this.stock = stock;
        this.description = description;
        this.status = status;
        this.image = image;
        this.categories = categories;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryResponse> categories) {
        this.categories = categories;
    }
}
