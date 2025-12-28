package com.ecommerce.product.repositories;

import com.ecommerce.product.models.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByIdAsc();
    //Page<Product> findAll(Pageable pageable);
    boolean existsByName(String name);
    Optional<Product> findByName(String name);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN p.categories pc
        WHERE (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE CONCAT('%', LOWER(:name), '%'))
          AND (:status IS NULL OR p.status = :status)
          AND (:minPrice IS NULL OR p.price >= :minPrice)
          AND (:maxPrice IS NULL OR p.price <= :maxPrice)
          AND (:categoryId IS NULL OR pc.id.categoryId = :categoryId)
        """)
    Page<Product> filterProducts(
            @Param("name") String name,
            @Param("status") Boolean status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
