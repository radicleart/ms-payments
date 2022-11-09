package com.radicle.payments.common.conf.token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GaiaInterceptor implements HandlerInterceptor {

	private static final String POST = "POST";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	private static final String MGMNT_V2 = "mgmnt-v2";
	private static final String STX_ADDRESS = "STX_ADDRESS";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
			String path = request.getRequestURI();
			String stxAddress = request.getHeader(STX_ADDRESS);
			if (requiresAuthentication(request, path)) {
				request.getSession().setAttribute("username", stxAddress);
			}
			request.getSession().setAttribute("stxAddress", stxAddress);
		}
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	private boolean requiresAuthentication(HttpServletRequest request, String path) {
		boolean required = false;
		required = checkHttpMethod(request, path) || isManagementRequest(path);
		return required;
	}

	private boolean checkHttpMethod(HttpServletRequest request, String path) {
		// allow posts from websocket api calls but otherwise require the token.
		if (path.startsWith("/mesh/api-news/"))
			return false;
		String m = request.getMethod();
		return (m.equalsIgnoreCase(DELETE) || m.equalsIgnoreCase(PUT) || m.equalsIgnoreCase(POST));
	}

	private boolean isManagementRequest(String path) {
		return path.indexOf(MGMNT_V2) > -1;
	}
}
