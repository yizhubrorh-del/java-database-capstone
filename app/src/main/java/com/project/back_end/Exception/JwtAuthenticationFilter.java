package com.project.back_end.Exception;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.services.impl.TokenServiceImpl;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenServiceImpl tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Sin token: se deja pasar (endpoints públicos como login/registro).
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            String identifier = tokenService.extractEmail(jwt);
            String role = tokenService.extractRole(jwt);

            if (identifier == null || !tokenService.validateToken(jwt, role)) {
                sendUnauthorized(response, "Token inválido o usuario inexistente");
                return;
            }

            // Datos disponibles en los controllers con @RequestAttribute("userIdentifier") / ("userRole")
            request.setAttribute("userIdentifier", identifier);
            request.setAttribute("userRole", role);

        } catch (JwtException | IllegalArgumentException e) {
            sendUnauthorized(response, "Token inválido o expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Las peticiones preflight de CORS no llevan token
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(message)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
