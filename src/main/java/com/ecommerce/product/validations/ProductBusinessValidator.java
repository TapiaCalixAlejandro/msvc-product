package com.ecommerce.product.validations;

import com.ecommerce.product.clients.CategoryClient;
import com.ecommerce.product.exception.BusinessException;
import com.ecommerce.product.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductBusinessValidator {
    private static final Logger log = LoggerFactory.getLogger(ProductBusinessValidator.class);
    private final ProductRepository productRepository;
    public final CategoryClient categoryClient;

    public ProductBusinessValidator(ProductRepository productRepository, CategoryClient categoryClient) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
    }

    public void validateUniqueNameOnCreate(String name) {
        if (productRepository.existsByName(name)) {
            log.warn("Ya existe un producto con el nombre:{}", name);
            throw new BusinessException("Ya existe un producto con el nombre:" + name);
        }
    }

    public void validateUniqueNameOnUpdate(Long id, String name) {
        productRepository.findByName(name).ifPresent(exists -> {
            if (!exists.getId().equals(id)) {
                log.warn("La validación de unicidad de nombre falló. Nombre: '{}', ID de producto: {}", name, id);
                throw new BusinessException("El nombre del producto '" + name + "' ya está en uso por otro recurso.");
            }
            log.info("Validación de nombre único superada para la actualización del producto con ID: {}", id);
        });
    }
}
