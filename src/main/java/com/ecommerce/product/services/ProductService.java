package com.ecommerce.product.services;

import com.ecommerce.product.models.dtos.ProductRequest;
import com.ecommerce.product.models.dtos.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductResponse> listProducts();
    ProductResponse createProduct(ProductRequest product, MultipartFile file) throws IOException;
    ProductResponse findProduct(UUID id);
    ProductResponse updateProduct(UUID id, ProductRequest product, MultipartFile file) throws IOException;
    void deleteProduct(UUID id);
    //Page<ProductResponse> getAllPaged(Pageable pageable);
}
