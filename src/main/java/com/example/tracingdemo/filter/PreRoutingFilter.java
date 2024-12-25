package com.example.tracingdemo.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Ensures the filter runs before Spring’s internal filters
public class PreRoutingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Simulate a pre-routing failure if the query parameter is set
        System.out.println("Pre-routing failure triggered. Returning HTTP 412.");
        String simulatePreRouting = httpRequest.getParameter("simulatePreRouting");
        if ("true".equals(simulatePreRouting)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpStatus.PRECONDITION_FAILED.value());
            httpResponse.getWriter().write("Precondition failed before reaching routing layer.");
            return; // Stop further processing
        }

        // Proceed normally if no simulation
        chain.doFilter(request, response);
    }
}
