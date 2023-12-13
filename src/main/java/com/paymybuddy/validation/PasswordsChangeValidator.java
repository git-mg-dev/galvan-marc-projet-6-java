package com.paymybuddy.validation;

import com.paymybuddy.model.PasswordChange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsChangeValidator implements ConstraintValidator<PasswordsChange, PasswordChange> {

    @Override
    public boolean isValid(PasswordChange passwordChange, ConstraintValidatorContext constraintValidatorContext) {
        return passwordChange.getNewPassword().equals(passwordChange.getConfirmPassword());
    }
}
