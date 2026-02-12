package com.ticketing.entrainement.api;

import com.ticketing.entrainement.application.ports.security.TokenClaims;
import com.ticketing.entrainement.infrastructure.adapter.security.JjwtTokenProvider;
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
    private final JjwtTokenProvider jjwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JjwtTokenProvider jjwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jjwtTokenProvider = jjwtTokenProvider;
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

        String accessToken = jjwtTokenProvider.generateAccessToken(req.username(), roles);
        String refreshToken = jjwtTokenProvider.generateRefreshToken(req.username());

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jjwtTokenProvider.getAccessExpirationMinutes()
        );
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody @Valid RefreshRequest req) {
        TokenClaims tokenClaims = jjwtTokenProvider.parseRefreshClaims(req.refreshToken());

        if (!"refresh".equals(String.valueOf(tokenClaims.type()))) {
            throw new BadCredentialsException("Invalid refresh token type");
        }

        String username = tokenClaims.subject();

        List<String> roles = List.of("ROLE_ADMIN");

        String newAccessToken = jjwtTokenProvider.generateAccessToken(username, roles);
        String newRefreshToken = jjwtTokenProvider.generateRefreshToken(username);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jjwtTokenProvider.getAccessExpirationMinutes()
        );
    }
}
