package com.ecommerce.product.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {
    public static final String TRACE_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "traceId";
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Si no viene traceId de otro msvc lo reutilizamos, sino lo generamos
        String traceId = req.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank())
            traceId = UUID.randomUUID().toString();

        // Guardarlo en MDC -> Disponible para logs
        MDC.put(MDC_KEY, traceId);

        // Propagación entre msvc
        res.getHeader(TRACE_HEADER);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_KEY); // Evitar fugas de memoria en thread pool
        }
    }
}
