package com.matchhire.apigateway.filter;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class UserIdHttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper{

    private final String userId;
    public UserIdHttpServletRequestWrapper(HttpServletRequest request, String userId) {
        super(request);
        this.userId = userId;
    }

    @Override
    public String getHeader(String name) {
        if("X-User-Id".equalsIgnoreCase(name)) {
            return userId;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("X-User-Id".equalsIgnoreCase(name)) {
            return Collections.enumeration(Collections.singletonList(userId));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        names.add("X-User-Id");
        return Collections.enumeration(names);
    }
}
