package com.rakuten.felix.testsend.manager.validator;


import lombok.experimental.UtilityClass;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public final class Validator {
    private final javax.validation.Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * Validate object.
     *
     * @param objectToValidate Object to validate.
     * @throws ValidationException When validation fails.
     */
    public <T> void validate(T objectToValidate) throws ValidationException {
        final Set<ConstraintViolation<T>> errors = validator.validate(objectToValidate);
        if (!errors.isEmpty()) {
            final String errorMessages = errors.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", ", "Validation failed: ", ""));
            throw new ValidationException(errorMessages);
        }
    }
}
