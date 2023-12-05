package com.paymybuddy.configuration;

import com.paymybuddy.model.UserAccount;
import com.paymybuddy.model.UserStatus;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount userAccount = userRepository.findByEmail(email);

        if (userAccount == null) {
            throw new UsernameNotFoundException("No user found with email: " + email);
        } else if(userAccount.getStatus() == UserStatus.ENABLED && !userAccount.isOpenidconnectUser()) {
            //Connection with email/password for users registered with OpenIdConnect is disabled

            boolean enabled = true;
            boolean accountNonExpired = true;
            boolean credentialsNonExpired = true;
            boolean accountNonLocked = true;

            return new User(userAccount.getEmail(), userAccount.getPassword(),
                    enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
                    getGrantedAuthorities(userAccount.getStatus().toString()));
        } else {
            throw new UsernameNotFoundException("Login with OpenIdConnect");
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        return authorities;
    }
}
