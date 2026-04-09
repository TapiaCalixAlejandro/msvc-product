package com.ecommerce.product.mappers;

import com.ecommerce.product.models.dtos.CategoryResponse;
import com.ecommerce.product.models.dtos.ProductRequest;
import com.ecommerce.product.models.dtos.ProductResponse;
import com.ecommerce.product.models.entities.Product;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    /**
     * Convierte ProductRequest a Product (entidad).
     * No asigna productId en el ProductCategoryId porque se resolverá al persistir.
     */
    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setImage(request.getImage());
        product.setDescription(request.getDescription());
        // Mappear categorias
        if (request.getCategoryIds() != null) {
            request.getCategoryIds().forEach(product::addCategory);
        }

        return product;
    }

    /**
     * Convierte entidad Product + categorías remotas (collection) a ProductResponse.
     * Acepta Collection<CategoryResponse> para ser flexible (List, Set, etc.).
     */
    public ProductResponse toResponse(Product product, Collection<CategoryResponse> categories) {
        // Construir response base
        Set<CategoryResponse> categorySet =
                categories == null ? Collections.emptySet() : new HashSet<>(categories);

        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setSku(product.getSku());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setImage(product.getImage());
        response.setDescription(product.getDescription());
        response.setCategories(categorySet);

        return response;
    }
}
