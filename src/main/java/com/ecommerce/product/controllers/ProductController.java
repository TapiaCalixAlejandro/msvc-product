package com.ecommerce.product.controllers;

import com.ecommerce.product.models.dtos.ProductRequest;
import com.ecommerce.product.models.dtos.ProductResponse;
import com.ecommerce.product.services.ProductSearchService;
import com.ecommerce.product.services.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final ProductSearchService searchService;

    public ProductController(ProductService productService, ProductSearchService searchService) {
        this.productService = productService;
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list() {
        return ResponseEntity.ok(productService.listProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> detail(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findProduct(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request, file));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestPart("product") ProductRequest request,
            @RequestPart(value = "image") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.updateProduct(id, request, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "") String sortDir
    ) {
        log.info("➡ sortBy={}, sortDir={}", sortBy, sortDir);
        Page<ProductResponse> result = searchService.filterProducts(name, status, minPrice, maxPrice, categoryId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(result);
    }
}
