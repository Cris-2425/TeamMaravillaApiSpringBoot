package com.teammaravilla.api.auth;

import com.teammaravilla.api.user.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService tokens;
    private final UserRepository users;
    private final AuthenticationEntryPoint unauthorized;

    public JwtAuthenticationFilter(JwtService tokens, UserRepository users, AuthenticationEntryPoint unauthorized) {
        this.tokens = tokens;
        this.users = users;
        this.unauthorized = unauthorized;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.AUTHORIZATION));
        if (!headers.isEmpty()) {
            try {
                if (headers.size() != 1 || !headers.getFirst().regionMatches(true, 0, "Bearer ", 0, 7)) {
                    throw new BadCredentialsException("Token inválido");
                }
                String token = headers.getFirst().substring(7);
                UUID userId = tokens.validateAndGetUserId(token);
                if (!users.existsById(userId)) throw new BadCredentialsException("Token inválido");
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
                SecurityContextHolder.clearContext();
                unauthorized.commence(request, response, new BadCredentialsException("Token inválido"));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
