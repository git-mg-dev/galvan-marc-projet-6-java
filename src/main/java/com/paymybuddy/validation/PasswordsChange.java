package com.paymybuddy.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsChangeValidator.class)
public @interface PasswordsChange {

    String message() default "Passwords must be identical";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
