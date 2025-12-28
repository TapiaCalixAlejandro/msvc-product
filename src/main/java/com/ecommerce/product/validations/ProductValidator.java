package com.ecommerce.product.validations;

import com.ecommerce.product.models.dtos.CategoryResponse;
import com.ecommerce.product.models.dtos.ProductRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ProductValidator {
    private final ProductBusinessValidator productBusinessValidator;
    private final ProductImageValidator productImageValidator;
    private final  ProductCategoryValidator productCategoryValidator;

    public ProductValidator(
            ProductBusinessValidator productBusinessValidator,
            ProductImageValidator productImageValidator,
            ProductCategoryValidator productCategoryValidator
    ) {
        this.productBusinessValidator = productBusinessValidator;
        this.productImageValidator = productImageValidator;
        this.productCategoryValidator = productCategoryValidator;
    }

    /**
     * @param request dto de productos
     * @param file imagen de producto
     * Validación de creación de producto
     */
    public List<CategoryResponse> validateOnCreate(ProductRequest request, MultipartFile file) {
        productImageValidator.validate(file);
        productBusinessValidator.validateUniqueNameOnCreate(request.getName());
        return productCategoryValidator.validateCategoryIds(request.getCategoryIds());
    }

    /**
     * @param id
     * @param request
     * @param file
     * @return
     */
    public List<CategoryResponse> validateOnUpdate(Long id, ProductRequest request, MultipartFile file) {
        productImageValidator.validate(file);
        productBusinessValidator.validateUniqueNameOnUpdate(id, request.getName());
        return productCategoryValidator.validateCategoryIds(request.getCategoryIds());
    }
}
