/*
package com.ecommerce.product.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

*/
/**
 * Filter que agrega un traceId (correlation id) a MDC por request.
 * - Si el cliente envía X-Trace-Id lo reutiliza.
 * - Si no, genera un UUID.
 *//*

@Component
public class CorrelationIdFilter implements Filter {
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String MDC_TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            //  Intentar leer el traceId del header entrante
            String traceId = httpRequest.getHeader(HEADER_TRACE_ID);

            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
            //  Guardar el MDC para que este disponible en los logs
            MDC.put(MDC_TRACE_ID, traceId);

            //  Exponer traceId en la respuesta
            httpResponse.setHeader(HEADER_TRACE_ID, traceId);

            //  Continuar la cadena
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
*/
