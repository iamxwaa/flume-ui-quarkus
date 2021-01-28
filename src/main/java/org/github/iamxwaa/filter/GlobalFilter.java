package org.github.iamxwaa.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.github.iamxwaa.utils.EnvUtils;

public class GlobalFilter implements Filter {
    private static final Log LOG = LogFactory.getLog(GlobalFilter.class);

    public static final String SESSION_AUTH_TOKEN = "session_auth_token";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (EnvUtils.getEnvConfig().isAuth()) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            String token = (String) req.getSession().getAttribute(SESSION_AUTH_TOKEN);
            if (StringUtils.isEmpty(token)) {
                LOG.warn("无页面访问权限");
                resp.sendError(403, "无页面访问权限.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
