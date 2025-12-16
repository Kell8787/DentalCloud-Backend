package com.dentalcloud.dentalcloudbackend.security;

import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/*
    CustomUserDetailsService is responsible for loading user-specific data during authentication.
    It implements the UserDetailsService interface provided by Spring Security.
    The loadUserByUsername method retrieves a User entity from the database using the provided email.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /*
        Loads the user by email and constructs a UserDetails object for authentication.
        @param email The email of the user to be loaded.
        @return UserDetails object containing user information and authorities.
        @throws UsernameNotFoundException if the user with the given email is not found.
     */
    @Override
    public @NonNull  UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .build();
    }
}