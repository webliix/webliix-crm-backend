package com.webliix.security.service;

import com.webliix.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                String roleName = role.getName().toUpperCase();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                authorities.add(new SimpleGrantedAuthority(roleName));

                if ("SUPER_ADMIN".equals(roleName)) {
                    authorities.add(new SimpleGrantedAuthority("*"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                }
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                true,
                true,
                true,
                authorities
        );
    }
}
