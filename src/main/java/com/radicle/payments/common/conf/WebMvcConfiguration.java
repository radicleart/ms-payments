package com.radicle.payments.common.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.radicle.payments.common.conf.token.GaiaInterceptor;

@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Autowired
	JWTHandlerInterceptor jwtInjectedInterceptor;
    @Autowired GaiaInterceptor gaiaInterceptor;

	@Override
    public void configurePathMatch(PathMatchConfigurer matcher) {
    }
    
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInjectedInterceptor).addPathPatterns("/**");
        registry.addInterceptor(gaiaInterceptor).addPathPatterns("/**");
	}
}
