package com.spring.task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.spring.task.web.formatter.UserFormatter;

@Configuration
public class SpringWebConfig  extends WebMvcConfigurerAdapter{
	
	@Autowired
    private UserFormatter userFormatter;
	
	@Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(userFormatter);
    }
}
