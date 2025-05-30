package com.together.server.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.together.server.support.error.ErrorType;
import com.together.server.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ApiResponse<Void> errorResponse = ApiResponse.error(ErrorType.FORBIDDEN);
        String body = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(body);
    }
}
