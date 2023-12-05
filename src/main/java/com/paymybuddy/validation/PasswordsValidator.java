package com.paymybuddy.validation;

import com.paymybuddy.model.RegisterInfo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsValidator implements ConstraintValidator<Passwords, RegisterInfo> {

    @Override
    public boolean isValid(RegisterInfo registerInfo, ConstraintValidatorContext constraintValidatorContext) {
        return registerInfo.getPassword().equals(registerInfo.getPasswordConfirm());
    }
}
