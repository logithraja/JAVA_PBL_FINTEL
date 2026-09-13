package com.example.expense_tracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter enforcing rate limits on authentication endpoints (5 req/min per IP)
 * and AI proxy endpoints (20 req/min per user / IP).
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Auth endpoints rate limiting (5 req/min per IP)
        if (isAuthEndpoint(path, method)) {
            String clientIp = getClientIp(request);
            if (!rateLimitingService.tryConsumeAuth(clientIp)) {
                sendRateLimitError(response, "Too many requests to authentication endpoints. Rate limit is 5 requests per minute.");
                return;
            }
        }

        // 2. AI endpoints rate limiting (20 req/min per user / IP)
        if (path.startsWith("/api/v1/ai")) {
            String userKey = resolveUserOrIpKey(request);
            if (!rateLimitingService.tryConsumeAi(userKey)) {
                sendRateLimitError(response, "Too many AI requests. Rate limit is 20 requests per minute.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String path, String method) {
        if ("POST".equalsIgnoreCase(method)) {
            return path.equals("/api/v1/user/login")
                    || path.equals("/api/v1/user")
                    || path.equals("/api/v1/user/forgot-password")
                    || path.equals("/api/v1/user/reset-password");
        }
        return false;
    }

    private String resolveUserOrIpKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Integer userId = jwtTokenProvider.getUserIdFromToken(token);
                    if (userId != null) {
                        return "user:" + userId;
                    }
                }
            } catch (Exception ignored) {}
        }
        return "ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"" + message + "\"}");
    }
}
