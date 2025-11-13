package org.example.student_testing.student.service;


import org.example.student_testing.student.entity.User;
import org.example.student_testing.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public  class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if(user == null)throw new UsernameNotFoundException("User not found");

        List<String> roles = userMapper.getRolesByUsername(username);
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        user.setAuthorities(authorities);

        System.out.println("Authorities for " + username + ": " + authorities);
        System.out.println("Is Locked (from DB): " + user.isLocked());

        return user;

    }
}
