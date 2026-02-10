package com.ticketing.entrainement.api;

import com.ticketing.entrainement.config.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid AuthRequest req) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtService.generateAccessToken(req.username(), roles);
        String refreshToken = jwtService.generateRefreshToken(req.username());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessExpirationMinutes()
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest req) {
        Claims claims = jwtService.parseRefreshClaims(req.refreshToken());

        if (!"refresh".equals(String.valueOf(claims.get("typ")))) {
            throw new BadCredentialsException("Invalid refresh token type");
        }

        String username = claims.getSubject();

        List<String> roles = List.of("ROLE_ADMIN");

        String newAccessToken = jwtService.generateAccessToken(username, roles);
        String newRefreshToken = jwtService.generateRefreshToken(username); // rotation simple

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getAccessExpirationMinutes()
        );
    }
}
