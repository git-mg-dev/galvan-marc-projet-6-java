package com.paymybuddy.service;

import com.paymybuddy.exceptions.UserAlreadyExistException;
import com.paymybuddy.model.RegisterInfo;
import com.paymybuddy.model.UserAccount;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserAccount findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) { return userRepository.findByEmail(email) != null; }

    public UserAccount registerNewUserAccount(RegisterInfo registerInfo, boolean openIdConnectUser) {
        if (emailExists(registerInfo.getEmail())) {
            throw new UserAlreadyExistException("An account with that email address already exists");
        }

        UserAccount userAccount = new UserAccount(registerInfo, openIdConnectUser);
        encodePassword(userAccount, registerInfo);

        return userRepository.save(userAccount);
    }

    private void encodePassword(UserAccount userAccount, RegisterInfo registerInfo){
        userAccount.setPassword(passwordEncoder.encode(registerInfo.getPassword()));
    }

}
