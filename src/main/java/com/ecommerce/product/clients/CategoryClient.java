package com.ecommerce.product.clients;

import com.ecommerce.product.models.dtos.CategoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// name = Nombre del msvc en Eureka
@FeignClient(name = "msvc-category", path = "/categories")
public interface CategoryClient {
    // Adaptación de tu metodo WebFlux a Feign
    // Feign serializará el Set<Long> a JSON y hará el POST automáticamente
    @PostMapping("/bulk")
    List<CategoryResponse> getCategoriesByIds(@RequestBody List<Long> ids);
}
