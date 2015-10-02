package com.freshcard.backend.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by willy on 18.01.15.
 */
public class HttpsEnforcer implements Filter {

    private FilterConfig filterConfig;

    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getHeader(X_FORWARDED_PROTO) != null) {
            if (request.getHeader(X_FORWARDED_PROTO).indexOf("https") != 0) {
                response.sendRedirect("https://" + request.getServerName() + (request.getPathInfo() != null ? request.getPathInfo() : ""));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    public void destroy() {
    }
}
