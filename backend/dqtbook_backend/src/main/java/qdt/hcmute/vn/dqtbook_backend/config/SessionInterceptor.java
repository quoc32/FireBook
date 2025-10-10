package qdt.hcmute.vn.dqtbook_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {
@Override
public boolean preHandle(HttpServletRequest request,
                         HttpServletResponse response,
                         Object handler) throws Exception {
    String uri = request.getRequestURI();
    System.out.println("👉 Interceptor check URI: " + uri);

    Object userId = request.getSession().getAttribute("userId");
    if (userId == null) {
        System.out.println("🚫 Blocked request to " + uri + " (no session)");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Login required!\"}");
        return false;
    }

    System.out.println("✅ Allowed request to " + uri);
    return true;
}

}
