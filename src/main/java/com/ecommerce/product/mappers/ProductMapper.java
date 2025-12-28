package com.ecommerce.product.mappers;

import com.ecommerce.product.models.dtos.CategoryResponse;
import com.ecommerce.product.models.dtos.ProductRequest;
import com.ecommerce.product.models.dtos.ProductResponse;
import com.ecommerce.product.models.entities.Product;
import org.springframework.stereotype.Component;

import java.util.Collection;
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

//        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
//            Set<ProductCategory> categories = new HashSet<>();
//            for (Long categoryId : request.getCategoryIds()) {
//                // Usamos el constructor que acepta (Product, CategoryId) para asegurar la relación bidireccional
//                // El ID compuesto se genera internamente en ese constructor.
//                ProductCategory pc = new ProductCategory(product, categoryId);
//                categories.add(pc);
//            }
//            product.setCategories(categories);
//        }
        return product;
    }

    /**
     * Convierte entidad Product + categorías remotas (collection) a ProductResponse.
     * Acepta Collection<CategoryResponse> para ser flexible (List, Set, etc.).
     */
    public ProductResponse toResponse(Product product, Collection<CategoryResponse> categories) {
        // Construir response base
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setSku(product.getSku());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setImage(product.getImage());
        response.setDescription(product.getDescription());

        if (categories != null && !categories.isEmpty()) {
            response.setCategories(new HashSet<>(categories));
        } else {
            Set<CategoryResponse> fallback = product.getCategories().stream()
                    .map(pc -> {
                        CategoryResponse cr = new CategoryResponse();
                        cr.setId(pc.getId().getCategoryId());
                        cr.setName("N/A");
                        return cr;
                    })
                    .collect(Collectors.toSet());
            response.setCategories(fallback);
        }
        return response;
    }
}
