package org.example.student_testing.config;

import org.example.student_testing.student.service.CustomUserDetailService;
import org.example.student_testing.student.service.UserService;
import org.example.student_testing.test.dto.LoginHistoryDTO;
import org.example.student_testing.test.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class LoginSuccessListener {


    @Autowired
    private LoginHistoryService loginService;



    @EventListener
    public void onLoginSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();

        // Lấy thông tin chi tiết từ principal
        Object principal = event.getAuthentication().getPrincipal();
        String fullName = username;
        String role = "UNKNOWN";

        if (principal instanceof UserDetails userDetails) {
            fullName = userDetails.getUsername(); // hoặc getFullName nếu có
            role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("UNKNOWN");
        }

        String userAgent = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getHeader("User-Agent");
        String ip = ((WebAuthenticationDetails) event.getAuthentication().getDetails()).getRemoteAddress();

        LoginHistoryDTO dto = new LoginHistoryDTO();
        dto.setUsername(username);
        dto.setFullName(fullName);
        dto.setRole(role);
        dto.setLoginTime(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        dto.setIpAddress(ip);
        dto.setUserAgent(userAgent);

        loginService.save(dto);
    }
}
