package com.ecommerce.product.repositories;

import com.ecommerce.product.models.entities.ProductCategory;
import com.ecommerce.product.models.entities.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {
    // Metodo custom para borrar relaciones por ID del producto
    @Transactional
    @Modifying
    @Query("DELETE FROM ProductCategory pc WHERE pc.product.id = :productId")
    void deleteAllByProductId(UUID productId);
}