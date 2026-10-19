package com.bembosevents.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ClientRequestIdentityService {

    private static final String UNKNOWN_CLIENT =
            "unknown";

    public String resolve(
            HttpServletRequest request) {

        if (request == null) {
            return UNKNOWN_CLIENT;
        }

        String remoteAddress =
                request.getRemoteAddr();

        if (remoteAddress == null
                || remoteAddress.isBlank()) {

            return UNKNOWN_CLIENT;
        }

        return remoteAddress.trim();
    }
}
