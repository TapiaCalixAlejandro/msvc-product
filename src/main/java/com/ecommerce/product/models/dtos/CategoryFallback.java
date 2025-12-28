package com.ecommerce.product.models.dtos;

public class CategoryFallback {
    private String message;

    public CategoryFallback() {
    }

    public CategoryFallback(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
