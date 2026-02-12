package com.ticketing.entrainement.config;

import com.ticketing.entrainement.application.ports.security.TokenClaims;
import com.ticketing.entrainement.infrastructure.adapter.security.JjwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JjwtTokenProvider jjwtTokenProvider;

    public JwtAuthenticationFilter(JjwtTokenProvider jjwtTokenProvider) {
        this.jjwtTokenProvider = jjwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authHeader.substring(7);
            try {
                TokenClaims tokenClaims = jjwtTokenProvider.parseAccessClaims(token);

                if (!"access".equals(String.valueOf(tokenClaims.type()))) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = tokenClaims.subject();

                if (username != null && !username.isBlank()) {
                    List<String> roles = tokenClaims.roles();
                    if (roles == null) roles = Collections.emptyList();

                    var authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
