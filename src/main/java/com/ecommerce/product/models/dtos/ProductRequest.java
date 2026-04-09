package com.ecommerce.product.models.dtos;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public class ProductRequest {
    @NotBlank(message = "El nombre del producto es obligatorio.")
    @Size(min = 3, max = 150, message = "El nombre del producto debe tener entre 3 y 150 caracteres.")
    @Column(nullable = false)
    private String name;
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0.")
    private double price;
    @NotBlank(message = "El sku es obligatorio.")
    private String sku;
    private Integer stock;
    private boolean status;
    @NotBlank(message = "La descripción del producto es obligatoria.")
    @Size(min = 10, max = 200, message = "La descripción del producto debe tener entre 3 y 150 caracteres.")
    private String description;
    private String image;
    @NotEmpty(message = "Debe asociarse al menos una categoría al producto.")
    private List<@NotNull(message = "El ID de la categoría no puede ser nulo.") UUID> categoryIds;

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

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
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

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
