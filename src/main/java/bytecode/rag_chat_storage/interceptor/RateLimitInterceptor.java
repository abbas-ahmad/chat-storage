package bytecode.rag_chat_storage.interceptor;

import bytecode.rag_chat_storage.service.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Use X-User-ID header as key
        String userId = request.getHeader("X-User-ID");
        if (userId == null || userId.isEmpty()) {
            userId = request.getRemoteAddr(); // fallback to IP
        }

        Bucket bucket = rateLimiterService.resolveBucket(userId);

        if (bucket.tryConsume(1)) {
            return true; // allow request
        } else {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return false; // block request
        }
    }
}
