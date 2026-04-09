package com.ecommerce.product.validations;

import com.ecommerce.product.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Component
public class ProductImageValidator {
    private static final Logger log = LoggerFactory.getLogger(ProductImageValidator.class);
    private static final long MAX_SIZE = 2 * 1024 *1024;

    public void validateCreate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("La imagen es obligatoria al crear una categoría.");
            throw new ValidationException(Collections.singletonList("La imagen es obligatoria al crear una categoría."));
        }
        validateCommomRules(file);
    }

    public void validateUpdate(MultipartFile file) {
        if (file == null || file.isEmpty())
            return; // el update puede no venir
        validateCommomRules(file);
    }

    public void validateCommomRules(MultipartFile file){
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg") ||
                        contentType.equalsIgnoreCase("image/jpg") ||
                        contentType.equalsIgnoreCase("image/png"))) {
            log.error("Solo se permiten imagenes JPEG, JPG o PNG.");
            throw new ValidationException(Collections.singletonList("Solo se permiten imagenes JPEG, JPG o PNG."));
        }

        if (file.getSize() > MAX_SIZE) {
            log.error("La imagen no debe superar los 2 MB.");
            throw new ValidationException(Collections.singletonList("La imagen no debe superar los 2 MB."));
        }
    }
}
