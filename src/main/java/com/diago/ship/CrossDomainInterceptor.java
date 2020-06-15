package com.diago.ship;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 解决跨域问题的拦截器
 *
 * @author Diago
 */
public class CrossDomainInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse rsp, Object o) throws Exception {
        rsp.setHeader("Access-Control-Allow-Methods", "PUT,POST,PATCH,GET,DELETE,OPTIONS");
        return true;
    }

}
