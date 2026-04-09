package com.ecommerce.product.validations;

import com.ecommerce.product.clients.CategoryClient;
import com.ecommerce.product.exception.BusinessException;
import com.ecommerce.product.exception.ExternalServiceException;
import com.ecommerce.product.exception.ValidationException;
import com.ecommerce.product.models.dtos.CategoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProductCategoryValidator {
    private static final Logger log = LoggerFactory.getLogger(ProductCategoryValidator.class);
    private final CategoryClient categoryClient;

    public ProductCategoryValidator(CategoryClient categoryClient) {
        this.categoryClient = categoryClient;
    }

    public List<CategoryResponse> validateCategoryIds(List<UUID> categoryIds) {
        // Validar presencía
        if (categoryIds == null || categoryIds.isEmpty()) {
            log.warn("El producto debe tener al menos una categoría.");
            throw new BusinessException("Debe asociarse al menos una categoría al producto");
        }

        // Validar duplicados antes del request remoto
        Set<UUID> unique = new HashSet<>(categoryIds);
        if (unique.size() != categoryIds.size()) {
            log.warn("La lista de categorias contiene valores duplicados.");
            throw new ValidationException(Collections.singletonList("La lista de categorias contiene valores duplicados."));
        }

        // Consumir msvc-category
        List<CategoryResponse> externalCategories;
        try {
            externalCategories = categoryClient.getCategoriesByIds(categoryIds);
        } catch (Exception e) {
            log.error("No se pudo obtener categorias desde msvc-category:{}", e.getMessage());
            throw new ExternalServiceException("msvc-category", e);
        }

        // Validar existencia real
        if (externalCategories == null || externalCategories.size() != categoryIds.size()) {
            log.error("Una o más categorías no existen según msvc-category.");
            throw new BusinessException("Una o más categorias no existen según msvc-category");
        }

        log.info("Categorias validas: {}", categoryIds);
        return externalCategories;
    }
}
