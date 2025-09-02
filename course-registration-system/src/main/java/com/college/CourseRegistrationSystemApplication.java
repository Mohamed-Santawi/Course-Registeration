package com.college;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;

@SpringBootApplication
public class CourseRegistrationSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseRegistrationSystemApplication.class, args);
    }

    // --- Session Listener Demo ---
    @WebListener
    public static class DemoSessionListener implements HttpSessionListener {
        @Override
        public void sessionCreated(HttpSessionEvent se) {
            System.out.println("[Listener] Session created: " + se.getSession().getId());
        }
        @Override
        public void sessionDestroyed(HttpSessionEvent se) {
            System.out.println("[Listener] Session destroyed: " + se.getSession().getId());
        }
    }
    // --- Filter Demo ---
    @Component
    public static class DemoRequestLoggingFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest req = (HttpServletRequest) request;
            System.out.println("[Filter] Request URI: " + req.getRequestURI());
            chain.doFilter(request, response);
        }
    }
}