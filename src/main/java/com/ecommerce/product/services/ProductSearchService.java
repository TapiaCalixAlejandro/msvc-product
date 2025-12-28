package com.ecommerce.product.services;

import com.ecommerce.product.models.dtos.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductSearchService {
    Page<ProductResponse> filterProducts(
            String name,
            Boolean status,
            Double minPrice,
            Double maxPrice,
            Long categoryId,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
