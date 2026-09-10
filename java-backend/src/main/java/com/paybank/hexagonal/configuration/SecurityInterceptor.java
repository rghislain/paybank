package com.paybank.hexagonal.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    
    //ThreadLocal permet de stocker le rôle de l'utilisateur pour la requête en cours
    private static final ThreadLocal<String> evaluationContext = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String role = request.getHeader("X-Auth-Role");
        evaluationContext.set(role != null ? role.toUpperCase() : "ANONYMOUS");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        evaluationContext.remove(); //nettoyage après la requête
    }

    public static String getContextRole() {
        return evaluationContext.get();
    }
}