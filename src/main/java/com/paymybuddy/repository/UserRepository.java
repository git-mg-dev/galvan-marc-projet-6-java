package com.paymybuddy.repository;

import com.paymybuddy.model.UserAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<UserAccount, Integer> {

    UserAccount findByEmail(String email);
}
