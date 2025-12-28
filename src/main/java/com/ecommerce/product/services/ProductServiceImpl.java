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

        if (products.isEmpty())
            throw new ResourceNotFoundException("No hay productos registrados.");

        // Mapeo de productos a DTO´s
        return products.stream()
                .map(product -> {
                    // Extraer IDs de Categorias
                    List<Long> catIds = product.getCategories().stream()
                            .map(pc -> pc.getId().getCategoryId())
                            .collect(Collectors.toList());

                    List<CategoryResponse> categories = new ArrayList<>();

                    // Llamada sincrona (Bloqueante) al msvc de categorias
                    /*if (!catIds.isEmpty()) {
                        try {
                            categories = categoryClient.getCategoriesByIds(catIds);
                        } catch (Exception e) {
                            log.warn("FALLBACK aplicado via FallbackFactory: msvc-category no disponible");
                            categories = categoryClient.getCategoriesByIds(catIds); // ahora sí cae al Fallback
                        }
                    }*/
                    // SIMPLIFICADO: Feign automáticamente usará el Fallback si msvc-category está caído
                    if (!catIds.isEmpty()) {
                        // Esta llamada usará automáticamente el Fallback si el servicio está caído
                        categories = fallbackService.getCategoriesByIds(catIds);
                        //categories = categoryClient.getCategoriesByIds(catIds);
                    }
                    return productMapper.toResponse(product, categories);
                }).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ProductResponse createProduct(ProductRequest request, MultipartFile file) {
        log.info("Iniciando la creación de un nuevo producto.");

        // Validación de negocio
//        if (productRepository.existsByName(request.getName())) {
//            log.error("Ya existe un producto con el nombre {}", request.getName());
//            throw new BusinessException("Ya existe un producto con el nombre: " + request.getName());
//        }
        List<CategoryResponse> categories = productValidator.validateOnCreate(request, file);

        /* Convertir DTO en Entidad */
        Product product = productMapper.toEntity(request);
        product.setCategories(new HashSet<>());
        Product saveProduct = productRepository.save(product);

        //  Si hay una imagen la guardamos en el sistema de archivos
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = handleImageUpload(file, null, saveProduct.getId());
                product.setImage(fileName);
                productRepository.save(saveProduct);
                log.info("Imagen guardada correctamente para el producto: {}", saveProduct.getName());
            } catch (Exception ex) {
                log.error("Error al guardar la imagen del producto {}:{}", saveProduct.getName(), ex.getMessage());
                throw new BusinessException("Error al guardar la imagen." + ex.getMessage(), ex);
            }
        }

        log.info("Asignar categorias al producto: {}", request.getName());
        /* Asignar categorias */
        assignCategories(saveProduct, request.getCategoryIds());
        log.info("Categorias asignadas correctamente.");

        // Obtener categorías remotas para el response
        /*List<CategoryResponse> categories;
        try {
            categories = categoryClient.getCategoriesByIds(request.getCategoryIds()).collectList().block();
        } catch (Exception ex) {
            log.error("Error al consultar servicio de categorias.");
            throw new ExternalServiceException("Error al consultar servicio de categorias.", ex);
        }*/

        log.info("Producto {} creado con exito", saveProduct.getName());
        return productMapper.toResponse(saveProduct, categories);
    }

    /**
     * Busca un producto por ID.
     */
    @Transactional(readOnly = true)
    @Override
    public ProductResponse findProduct(Long id) {
        log.info("Buscando producto reactivamente por ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("No se encontró un producto, ID:{}", id);
                    return new ResourceNotFoundException("Producto no encontrado, ID:" +id);
                });

        List<Long> catIds = product.getCategories().stream()
                .map(pc -> pc.getId().getCategoryId())
                .collect(Collectors.toList());

        List<CategoryResponse> categories = new ArrayList<>();

        if (!catIds.isEmpty()) {
//            try {
//                categories = categoryClient.getCategoriesByIds(catIds);
//            } catch (Exception e) {
//                throw new ExternalServiceException("Error al consultar categorias.", e);
//            }
            categories = fallbackService.getCategoriesByIds(catIds);
        }
        return productMapper.toResponse(product, categories);
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile file) {
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

        /*List<CategoryResponse> categories;
        try {
            categories = categoryClient
                    .getCategoriesByIds(request.getCategoryIds())
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Error al consultar servicio de categorias.");
            throw new ExternalServiceException("Error al consultar servicio de categorias.", e);
        }*/

        log.info("El producto {} fue modificado con éxito.", exists.getName());
        return productMapper.toResponse(update, categories);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        log.info("Iniciando proceso de eliminación.");
        if (!productRepository.existsById(id)) {
            log.error("No se encontró un producto con ID: {}", id);
            throw new ResourceNotFoundException("No existe un producto con el ID: " + id);
        }
        productRepository.deleteById(id);
    }

    /* Metodo reutilizable para manejar la logica de la imagen (Creacion y modificacion) */
    private String handleImageUpload(MultipartFile file, String oldImageName, Long id) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.debug("Directorio de carga para categorias creado: {}", uploadPath);
        }
        // Eliminar imagen anterior si existe
        if (oldImageName != null) {
            Path oldImagePath = uploadPath.resolve(Paths.get(oldImageName).getFileName().toString());
            Files.deleteIfExists(oldImagePath);
            log.debug("Imagen anterior eliminada: {}", oldImagePath);
        }
        //  Guardar nueva imagen
        String filename = "product_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Nueva imagen guardada en: {}", filePath);

        return filename;
    }

    /* Metodo centralizado para asignar categorias a productos */
    private void assignCategories(Product product, List<Long> categoryIds) {
        pcRepository.deleteAllByProductId(product.getId());
        Set<ProductCategory> relations = new HashSet<>();
        for (Long catId : categoryIds) {
            relations.add(new ProductCategory(product, catId));
        }
        pcRepository.saveAll(relations);
        product.setCategories(relations);
    }
}
