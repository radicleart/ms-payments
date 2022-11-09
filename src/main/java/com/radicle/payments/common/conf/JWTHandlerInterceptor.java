package com.radicle.payments.common.conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class JWTHandlerInterceptor implements HandlerInterceptor {

    @Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
        try {
	         // printHeaders(request);
	         // printHeaders(response);
        } catch (Exception e) {
            throw e;
        }
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

}
