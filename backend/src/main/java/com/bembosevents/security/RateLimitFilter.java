package com.bembosevents.security;

import com.bembosevents.domain.RateLimitScope;
import com.bembosevents.dto.common.ApiErrorResponse;
import com.bembosevents.exception.RateLimitExceededException;
import com.bembosevents.service.RateLimitEnforcementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
public class RateLimitFilter
        extends OncePerRequestFilter {

    private static final String LOGIN_PATH =
            "/api/auth/login";

    private static final String PASSWORD_RECOVERY_PATH =
            "/api/auth/password/solicitar";

    private static final String EMAIL_VERIFICATION_PATH =
            "/api/auth/correo/reenviar";

    private static final String MFA_VERIFY_PATH =
            "/api/auth/mfa/verificar";

    private static final String MFA_RECOVERY_PATH =
            "/api/auth/mfa/recuperacion";

    private final RateLimitEnforcementService
            rateLimitEnforcementService;

    private final ClientRequestIdentityService
            clientRequestIdentityService;

    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimitEnforcementService rateLimitEnforcementService,
            ClientRequestIdentityService clientRequestIdentityService,
            ObjectMapper objectMapper) {

        this.rateLimitEnforcementService =
                rateLimitEnforcementService;

        this.clientRequestIdentityService =
                clientRequestIdentityService;

        this.objectMapper =
                objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        return resolveScope(
                request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        RateLimitScope scope =
                resolveScope(
                        request);

        if (scope == null) {

            filterChain.doFilter(
                    request,
                    response);

            return;
        }

        String subject =
                clientRequestIdentityService.resolve(
                        request);

        try {
            rateLimitEnforcementService.enforce(
                    scope,
                    subject);
        }
        catch (RateLimitExceededException ex) {

            writeTooManyRequests(
                    response,
                    ex);

            return;
        }

        filterChain.doFilter(
                request,
                response);
    }

    private RateLimitScope resolveScope(
            HttpServletRequest request) {

        if (request == null
                || !"POST".equalsIgnoreCase(
                        request.getMethod())) {

            return null;
        }

        String uri =
                request.getRequestURI();

        if (LOGIN_PATH.equals(uri)) {
            return RateLimitScope.LOGIN;
        }

        if (PASSWORD_RECOVERY_PATH.equals(uri)) {
            return RateLimitScope.PASSWORD_RECOVERY;
        }

        if (EMAIL_VERIFICATION_PATH.equals(uri)) {
            return RateLimitScope.EMAIL_VERIFICATION;
        }

        if (MFA_VERIFY_PATH.equals(uri)
                || MFA_RECOVERY_PATH.equals(uri)) {

            return RateLimitScope.MFA_CHALLENGE;
        }

        return null;
    }

    private void writeTooManyRequests(
            HttpServletResponse response,
            RateLimitExceededException ex)
            throws IOException {

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value());

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                Long.toString(
                        ex.getRetryAfterSeconds()));

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE);

        response.setCharacterEncoding(
                "UTF-8");

        ApiErrorResponse body =
                new ApiErrorResponse(
                        LocalDateTime.now(
                                ZoneOffset.UTC),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too Many Requests",
                        ex.getMessage(),
                        Map.of());

        objectMapper.writeValue(
                response.getOutputStream(),
                body);
    }
}
