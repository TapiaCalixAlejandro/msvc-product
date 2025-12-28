package com.ecommerce.product.repositories;

import com.ecommerce.product.models.entities.ProductCategory;
import com.ecommerce.product.models.entities.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    // Metodo custom para borrar relaciones por ID del producto
    @Modifying
    @Query("DELETE FROM ProductCategory pc WHERE pc.id.productId = :productId")
    void deleteAllByProductId(Long productId);
}