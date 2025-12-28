package com.ecommerce.product.validations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Component
public class ProductImageValidator {
    private static final Logger log = LoggerFactory.getLogger(ProductImageValidator.class);

    public Optional<String> validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("La imagen es obligatoria para crear producto.");
            return Optional.of("La imagen es obligatoria para crear un producto.");
        }

        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !(contentType.equalsIgnoreCase("image/png") || contentType.equalsIgnoreCase("image/jpg") ||
                    contentType.equalsIgnoreCase("image/jpeg"))) {
                log.warn("El producto solo acepta imagenes en formato de tipo PNG, JPG y JEPG.");
                return Optional.of("Solo se permiten imagenes de tipo PNG, JPG y JEPG");
            }

            if (file.getSize() > 2 * 1024 * 1024) {
                log.warn("La imagen no debe pesar más de 2MB.");
                return Optional.of("La imagen no debe superar los 2MB");
            }
        }
        return Optional.empty();
    }
}
