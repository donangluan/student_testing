package org.example.student_testing.student.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class User implements UserDetails {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String roleCode;
    private LocalDateTime  createdAt;
    private LocalDateTime  updatedAt;

    private boolean isLocked;

    private List<GrantedAuthority> authorities;


    @Override
    public boolean isAccountNonLocked() {

        return !this.isLocked;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }


    @Override
    public String getPassword() {
        return this.password;
    }


    @Override
    public String getUsername() {
        return this.username;
    }



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

}
