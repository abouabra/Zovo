package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.UserService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService userService;

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<? extends ApiResponse<?>> testAdminEligibility(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return userService.testAdminEligibility(loggedInUser);
    }

    @GetMapping("/user-sessions/")
    public ResponseEntity<? extends ApiResponse<?>> getSessionInfo(HttpSession session) {
        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", session.getId());
        info.put("creationTime", new Date(session.getCreationTime()));
        info.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
        info.put("MaxAge", session.getMaxInactiveInterval());
        info.put("sessionStatus", session.getAttribute("SPRING_SECURITY_CONTEXT"));
        return ApiResponse.success(info);
    }
}
