package com.project.back_end.Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.back_end.services.impl.TokenServiceImpl;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private TokenServiceImpl tokenService;
    @Mock private FilterChain filterChain;

    @Test
    void requestWithoutTokenPassesThrough() throws Exception {
        var filter = new JwtAuthenticationFilter(tokenService, new ObjectMapper());
        var request = new MockHttpServletRequest("GET", "/doctor");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void validBearerTokenSetsUserAttributesAndContinues() throws Exception {
        var filter = new JwtAuthenticationFilter(tokenService, new ObjectMapper());
        var request = new MockHttpServletRequest("GET", "/patient");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        var response = new MockHttpServletResponse();
        when(tokenService.extractEmail("good-token")).thenReturn("john@mail.com");
        when(tokenService.extractRole("good-token")).thenReturn("patient");
        when(tokenService.validateToken("good-token", "patient")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(request.getAttribute("userIdentifier")).isEqualTo("john@mail.com");
        assertThat(request.getAttribute("userRole")).isEqualTo("patient");
        verify(filterChain).doFilter(request, response);
    }
}
