package com.ecommerce.product.models.dtos;

public class CategoryResponse {
    private Long id;
    private String name;
    private String image;
    private Boolean status;
    private String description;

    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name, String image, Boolean status, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
}
