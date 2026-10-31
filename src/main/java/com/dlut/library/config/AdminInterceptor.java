package com.dlut.library.config;

import com.dlut.library.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        User user = request.getSession(false) == null
                ? null : (User) request.getSession(false).getAttribute("authUser");
        if (user != null && user.isAdmin()) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "仅管理员可访问后台");
        return false;
    }
}
