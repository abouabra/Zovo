package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.UserService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<UserResponseDTO> testAdminEligibility(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        UserResponseDTO dto = userService.testAdminEligibility(loggedInUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/user-sessions/")
    public ResponseEntity<Map<String, Object>> getSessionInfo(HttpSession session) {
        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", session.getId());
        info.put("creationTime", new Date(session.getCreationTime()));
        info.put("lastAccessedTime", new Date(session.getLastAccessedTime()));
        info.put("MaxAge", session.getMaxInactiveInterval());
        info.put("sessionStatus", session.getAttribute("SPRING_SECURITY_CONTEXT"));
        return ResponseEntity.ok(info);
    }
}
