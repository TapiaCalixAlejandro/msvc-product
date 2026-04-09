package com.ecommerce.product.services;

import com.ecommerce.product.clients.CategoryFallbackService;
import com.ecommerce.product.exception.BusinessException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mappers.ProductMapper;
import com.ecommerce.product.models.dtos.CategoryResponse;
import com.ecommerce.product.models.dtos.ProductRequest;
import com.ecommerce.product.models.dtos.ProductResponse;
import com.ecommerce.product.models.entities.Product;
import com.ecommerce.product.models.entities.ProductCategory;
import com.ecommerce.product.repositories.ProductCategoryRepository;
import com.ecommerce.product.repositories.ProductRepository;
import com.ecommerce.product.validations.ProductValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de Productos, utilizando programación reactiva (Project Reactor)
 * y encapsulando las operaciones bloqueantes de JPA y I/O en Schedulers dedicados.
 */
@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Value("${app.upload.dir}")
    private String uploadDir; // Directorio de carga de la imagen

    private final ProductRepository productRepository;
    private final ProductCategoryRepository pcRepository;
    private final CategoryFallbackService fallbackService;    // Inyección de Feign
    private final ProductMapper productMapper;
    private final ProductValidator productValidator;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductCategoryRepository pcRepository,
            CategoryFallbackService fallbackService,
            ProductMapper productMapper,
            ProductValidator productValidator
    ) {
        this.productRepository = productRepository;
        this.pcRepository = pcRepository;
        this.fallbackService = fallbackService;
        this.productMapper = productMapper;
        this.productValidator = productValidator;
    }

    /**
     * Consulta todos los productos, luego busca sus categorías de forma reactiva.
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> listProducts() {
        log.info("Consultando lista de productos reactivamente.");

        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No hay productos registrados.");
        }

        Set<UUID> allCategoryIds = products
                .stream()
                .flatMap(p -> p.getCategories().stream())
                        .map(pc -> pc.getCategoryId())
                        .collect(Collectors.toSet());

        Map<UUID, CategoryResponse> categoryMap = allCategoryIds.isEmpty()
                ? Collections.emptyMap()
                : fallbackService.getCategoriesByIds(new ArrayList<>(allCategoryIds))
                    .stream()
                    .collect(Collectors.toMap(CategoryResponse::getId, c -> c));

        return products.stream()
                .map(product -> {
                    List<CategoryResponse> categories = product.getCategories().stream()
                            .map(pc -> categoryMap.get(pc.getCategoryId()))
                            .filter(Objects::nonNull)
                            .toList();
                    return productMapper.toResponse(product, categories);
                })
                .toList();
    }

    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request, MultipartFile file) {
        log.info("Iniciando la creación de un nuevo producto.");

        List<CategoryResponse> categories = productValidator.validateOnCreate(request, file);

        /* Convertir DTO en Entidad */
        Product product = productMapper.toEntity(request);
        Product saveProduct = productRepository.save(product);

        //  Si hay una imagen la guardamos en el sistema de archivos
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = handleImageUpload(file, null, saveProduct.getId());
                saveProduct.setImage(fileName); // 🔥 clave
                productRepository.save(saveProduct);
                log.info("Imagen guardada correctamente para el producto: {}", product.getName());
            } catch (Exception ex) {
                log.error("Error al guardar la imagen del producto {}:{}", product.getName(), ex.getMessage());
                throw new BusinessException("Error al guardar la imagen." + ex.getMessage(), ex);
            }
        }
        product = productRepository.save(product);
        log.info("Asignar categorias al producto: {}", request.getName());
        /* Asignar categorias */
        assignCategories(product, request.getCategoryIds());
        log.info("Categorias asignadas correctamente.");

        log.info("Producto {} creado con exito", product.getName());
        return productMapper.toResponse(product, categories);
    }

    /**
     * Busca un producto por ID.
     */
    @Transactional(readOnly = true)
    @Override
    public ProductResponse findProduct(UUID id) {
        log.info("Buscando producto reactivamente por ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró un producto, ID:{}", id);
                    return new ResourceNotFoundException("Producto no encontrado, ID:" +id);
                });

        List<UUID> catIds = product.getCategories().stream()
                .map(pc -> pc.getCategoryId())
                .toList();

        List<CategoryResponse> categories = catIds.isEmpty()
                ? List.of()
                : fallbackService.getCategoriesByIds(catIds);

        return productMapper.toResponse(product, categories);
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request, MultipartFile file) {
        log.info("Iniciando el proceso de modificación del producto: {}", request.getName());
        Product exists = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró un producto con el ID: {}", id);
                    return new ResourceNotFoundException("El producto con el ID: " + id + " no existe.");
                });

        /* Validación de nombre único */
        List<CategoryResponse> categories = productValidator.validateOnUpdate(id, request, file);

        log.info("Actualizando campos del producto: {}", exists.getName());
        /* Actualizar campos */
        exists.setName(request.getName());
        exists.setPrice(request.getPrice());
        exists.setSku(request.getSku());
        exists.setStock(request.getStock());
        exists.setDescription(request.getDescription());
        exists.setStatus(request.getStatus());

        log.info("Validando imagen del producto.");
        /* Si se envia una nueva imagen se procesa */
        if (file != null && !file.isEmpty()) {
            try {
                String newImageName = handleImageUpload(file, exists.getImage(), id);
                exists.setImage(newImageName);
                log.info("Imagen actualizada para el producto {}", exists.getName());
            } catch (Exception e) {
                log.error("Error al actualizar la imagen del producto.");
                throw new BusinessException("Error al actualizar la imagen del producto.", e);
            }
        }

        Product update = productRepository.save(exists);

        log.info("Asignando/Actualizando categorias para el producto {}", exists.getName());
        /* Reutilizar el metodo para validar categorias */
        assignCategories(update, request.getCategoryIds());

        log.info("El producto {} fue modificado con éxito.", exists.getName());
        return productMapper.toResponse(update, categories);
    }

    @Transactional
    @Override
    public void deleteProduct(UUID id) {
        log.info("Iniciando proceso de eliminación.");
        if (!productRepository.existsById(id)) {
            log.error("No se encontró un producto con ID: {}", id);
            throw new ResourceNotFoundException("No existe un producto con el ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /* Metodo reutilizable para manejar la logica de la imagen (Creacion y modificacion) */
    private String handleImageUpload(MultipartFile file, String oldImageName, UUID id) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.debug("Directorio de carga para categorias creado: {}", uploadPath);
        }
        // Eliminar imagen anterior si existe
        if (oldImageName != null) {
            //Path oldImagePath = uploadPath.resolve(Paths.get(oldImageName).getFileName().toString());
            Files.deleteIfExists(uploadPath.resolve(oldImageName));
            log.debug("Imagen anterior eliminada: {}", oldImageName);
        }
        //  Guardar nueva imagen
        String filename = "product_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Nueva imagen guardada en: {}", filePath);

        return filename;
    }

    /* Metodo centralizado para asignar categorias a productos */
    private void assignCategories(Product product, List<UUID> categoryIds) {
        pcRepository.deleteAllByProductId(product.getId());
        if (categoryIds == null || categoryIds.isEmpty()) {
            product.setCategories(Collections.emptySet());
            return;
        }

        Set<ProductCategory> relations = categoryIds.stream()
                .map(catId -> new ProductCategory(product, catId))
                .collect(Collectors.toSet());

        pcRepository.saveAll(relations);
        product.setCategories(relations);
    }
}
