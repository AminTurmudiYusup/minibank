package com.example.minibank.ratelimit;

import com.example.minibank.common.exception.ErrorResponse;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        Optional<RateLimitPolicy> optionalPolicy =
                rateLimitService.findPolicy(method, path);

        // No configured policy: continue normally.
        if (optionalPolicy.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitPolicy policy = optionalPolicy.get();

        String clientId = resolveClientId(request);

        ConsumptionProbe probe =
                rateLimitService.consume(policy, clientId);

        if (probe.isConsumed()) {

            response.setHeader(
                    "X-RateLimit-Limit",
                    String.valueOf(policy.capacity())
            );

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens())
            );

            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(
                1,
                (probe.getNanosToWaitForRefill()
                        + 999_999_999L) / 1_000_000_000L
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setHeader(
                "Retry-After",
                String.valueOf(retryAfterSeconds)
        );

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                429,
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                path
        );

        objectMapper.writeValue(response.getWriter(), error);
    }

    private String resolveClientId(HttpServletRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            return "user:" + authentication.getName();
        }

        return "ip:" + request.getRemoteAddr();
    }
}
