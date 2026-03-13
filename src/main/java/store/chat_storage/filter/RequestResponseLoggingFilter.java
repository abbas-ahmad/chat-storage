
package store.chat_storage.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    private final Tracer tracer;

    public RequestResponseLoggingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip logging for actuator and swagger endpoints
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/actuator") ||
            requestUri.startsWith("/swagger-ui") ||
            requestUri.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        // Get trace context
        Span currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null ? currentSpan.context().traceId() : "N/A";
        String spanId = currentSpan != null ? currentSpan.context().spanId() : "N/A";

        // Add trace information to MDC for logging
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);

        try {
            // Log incoming request
            logRequest(wrappedRequest, traceId, spanId);

            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log outgoing response
            logResponse(wrappedResponse, traceId, spanId, duration);

            // Copy cached response content to actual response
            wrappedResponse.copyBodyToResponse();

            // Clean up MDC
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String traceId, String spanId) {
        Map<String, Object> requestLog = new HashMap<>();
        requestLog.put("type", "REQUEST");
        requestLog.put("traceId", traceId);
        requestLog.put("spanId", spanId);
        requestLog.put("method", request.getMethod());
        requestLog.put("uri", request.getRequestURI());
        requestLog.put("queryString", request.getQueryString());
        requestLog.put("remoteAddr", request.getRemoteAddr());
        requestLog.put("userId", request.getHeader("X-User-ID"));

        // Log headers (excluding sensitive ones)
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Mask sensitive headers
            if (headerName.equalsIgnoreCase("X-API-KEY") ||
                headerName.equalsIgnoreCase("Authorization")) {
                headers.put(headerName, "***MASKED***");
            } else {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        requestLog.put("headers", headers);

        // Log request body (if present and not too large)
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0 && content.length < 10000) {
            String body = new String(content, StandardCharsets.UTF_8);
            requestLog.put("body", body);
        } else if (content.length >= 10000) {
            requestLog.put("body", "[Body too large to log: " + content.length + " bytes]");
        }

        auditLogger.info("Incoming Request: {}", requestLog);
    }

    private void logResponse(ContentCachingResponseWrapper response, String traceId, String spanId, long duration) {
        Map<String, Object> responseLog = new HashMap<>();
        responseLog.put("type", "RESPONSE");
        responseLog.put("traceId", traceId);
        responseLog.put("spanId", spanId);
        responseLog.put("status", response.getStatus());
        responseLog.put("durationMs", duration);

        // Log response headers
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        responseLog.put("headers", headers);

        // Log response body (if present and not too large)
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0 && content.length < 10000) {
            String body = new String(content, StandardCharsets.UTF_8);
            responseLog.put("body", body);
        } else if (content.length >= 10000) {
            responseLog.put("body", "[Body too large to log: " + content.length + " bytes]");
        }

        auditLogger.info("Outgoing Response: {}", responseLog);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}