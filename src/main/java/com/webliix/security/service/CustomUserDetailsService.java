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
                    authorities.add(new SimpleGrantedAuthority("ALL"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    authorities.add(new SimpleGrantedAuthority("MANAGER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_HR"));
                    authorities.add(new SimpleGrantedAuthority("HR"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
                    authorities.add(new SimpleGrantedAuthority("EMPLOYEE"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    authorities.add(new SimpleGrantedAuthority("USER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
                    authorities.add(new SimpleGrantedAuthority("CLIENT"));
                    authorities.add(new SimpleGrantedAuthority("PROJECTS_CREATE"));
                    authorities.add(new SimpleGrantedAuthority("PROJECTS_VIEW"));
                    authorities.add(new SimpleGrantedAuthority("PROJECTS_EDIT"));
                    authorities.add(new SimpleGrantedAuthority("PROJECTS_DELETE"));
                    authorities.add(new SimpleGrantedAuthority("TICKETS_CREATE"));
                    authorities.add(new SimpleGrantedAuthority("TICKETS_VIEW"));
                    authorities.add(new SimpleGrantedAuthority("TICKETS_EDIT"));
                    authorities.add(new SimpleGrantedAuthority("TICKETS_DELETE"));
                    authorities.add(new SimpleGrantedAuthority("INVOICES_CREATE"));
                    authorities.add(new SimpleGrantedAuthority("INVOICES_VIEW"));
                    authorities.add(new SimpleGrantedAuthority("INVOICES_EDIT"));
                    authorities.add(new SimpleGrantedAuthority("INVOICES_DELETE"));
                    authorities.add(new SimpleGrantedAuthority("LEADS_CREATE"));
                    authorities.add(new SimpleGrantedAuthority("LEADS_VIEW"));
                    authorities.add(new SimpleGrantedAuthority("LEADS_EDIT"));
                    authorities.add(new SimpleGrantedAuthority("LEADS_DELETE"));
                    authorities.add(new SimpleGrantedAuthority("LEADS_CONVERT"));
                    authorities.add(new SimpleGrantedAuthority("CUSTOMERS_CREATE"));
                    authorities.add(new SimpleGrantedAuthority("CUSTOMERS_VIEW"));
                    authorities.add(new SimpleGrantedAuthority("CUSTOMERS_EDIT"));
                    authorities.add(new SimpleGrantedAuthority("CUSTOMERS_DELETE"));
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
