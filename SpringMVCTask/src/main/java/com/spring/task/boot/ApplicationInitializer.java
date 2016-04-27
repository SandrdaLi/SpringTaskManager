package com.spring.task.boot;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.spring.task.config.SpringDBConfig;
import com.spring.task.config.SpringWebConfig;

public class ApplicationInitializer implements WebApplicationInitializer {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		logger.info("===== Application is starting up! ========");

		AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(SpringWebConfig.class, SpringDBConfig.class);
        ctx.setServletContext(servletContext);
 
        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));
 
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
		servlet.setAsyncSupported(true);

		servlet.setMultipartConfig(new MultipartConfigElement("/tmp/servlet-uploads", 20848820, 418018841, 1048576));

		FilterRegistration.Dynamic filter = servletContext.addFilter("httpMethodFilter",
				"org.springframework.web.filter.HiddenHttpMethodFilter");
		filter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST), true, "dispatcher");
		filter.setAsyncSupported(true);
	}

	private AnnotationConfigWebApplicationContext getAnnotationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setConfigLocation("com.spring.task.config");
		return context;
	}

	private XmlWebApplicationContext getXMLContext() {
		XmlWebApplicationContext context = new XmlWebApplicationContext();
		context.setConfigLocation("/WEB-INF/spring/appServlet/servlet-context.xml");
		return context;
	}
}
