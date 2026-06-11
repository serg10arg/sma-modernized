package com.sma.licensing.utils;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Interceptor que añade el correlationId del UserContext a las cabeceras de
 * las llamadas HTTP salientes, propagando la trazabilidad hacia otros servicios.
 */
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String correlationId = UserContext.getCorrelationId();
        if (correlationId != null) {
            request.getHeaders().add(UserContext.CORRELATION_ID, correlationId);
        }
        return execution.execute(request, body);
    }
}
