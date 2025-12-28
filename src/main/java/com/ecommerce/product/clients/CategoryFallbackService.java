package com.ecommerce.product.clients;

import com.ecommerce.product.models.dtos.CategoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryFallbackService {
    private static final Logger log = LoggerFactory.getLogger(CategoryFallbackService.class);
    private final CategoryClient categoryClient;

    public CategoryFallbackService(CategoryClient categoryClient) {
        this.categoryClient = categoryClient;
    }

    // Al estar en otro Bean, Spring sí puede interceptar esta llamada
    @CircuitBreaker(name = "categoryCB", fallbackMethod = "fallbackCategory")
    public List<CategoryResponse> getCategoriesByIds(List<Long> ids) {
        return categoryClient.getCategoriesByIds(ids);
    }

    // Fallback debe tener la misma firma + Throwable
    public List<CategoryResponse> fallbackCategory(List<Long> ids, Throwable ex) {
        log.warn("FALLBACK ACTIVADO: msvc-category caído o lento. Error: {}", ex.getMessage());
        return ids.stream().map(id -> {
            CategoryResponse cr = new CategoryResponse();
            cr.setId(0L);
            cr.setName("N/A - Categoria no disponible");
            cr.setImage("NO-IMAGE.JPG");
            cr.setDescription("MSVC-CATEGORY Caido");
            cr.setStatus(false);
            return cr;
        }).collect(Collectors.toList());
    }
}
