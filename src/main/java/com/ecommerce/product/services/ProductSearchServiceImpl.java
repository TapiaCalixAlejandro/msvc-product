package com.ecommerce.product.services;

import com.ecommerce.product.clients.CategoryFallbackService;
import com.ecommerce.product.mappers.ProductMapper;
import com.ecommerce.product.models.dtos.CategoryResponse;
import com.ecommerce.product.models.dtos.ProductResponse;
import com.ecommerce.product.models.entities.Product;
import com.ecommerce.product.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryFallbackService fallbackService;

    public ProductSearchServiceImpl(
            ProductRepository productRepository,
            ProductMapper productMapper,
            CategoryFallbackService fallbackService
    ) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.fallbackService = fallbackService;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductResponse> filterProducts(
            String name,
            Boolean status,
            Double minPrice,
            Double maxPrice,
            Long categoryId,
            int page,
            int size,
            String sortBy,
            String sortDir
            ) {
        Pageable pageable;
        boolean hasSort = sortBy != null && !sortBy.isBlank()
                && sortDir != null && !sortDir.isBlank();

        if (hasSort) {
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            pageable = PageRequest.of(page, size, sort);
        } else {
            pageable = PageRequest.of(page, size); // sin ordenamiento
        }

        Page<Product> products = productRepository.filterProducts(name, status, minPrice, maxPrice, categoryId, pageable);

        return products.map(p -> {
            List<Long> catIds = p.getCategories().stream()
                    .map(pc -> pc.getId().getCategoryId())
                    .toList();
            List<CategoryResponse> categories = catIds.isEmpty()
                    ? List.of()
                    : fallbackService.getCategoriesByIds(catIds);
            return productMapper.toResponse(p, categories);
        });
    }
}
