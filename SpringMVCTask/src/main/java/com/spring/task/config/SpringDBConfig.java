package com.spring.task.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.spring.task.service.TaskService;
import com.spring.task.service.UserService;

@Configuration
@EnableTransactionManagement
public class SpringDBConfig {
	private static final Logger logger = LoggerFactory.getLogger(SpringDBConfig.class);

	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserService userService;

	@Autowired
	private TaskService taskService;

	@Bean
	DataSource getHsqlDatasource() {

		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
				.addScript("db-scripts/hsql/db-schema.sql").addScript("db-scripts/hsql/data.sql")
				.addScript("db-scripts/hsql/storedprocs.sql").addScript("db-scripts/hsql/functions.sql")
				.setSeparator("/").build();
		//
		// DriverManagerDataSource dataSource = new
		// DriverManagerDataSource().setDriverClassName("org.hsqldb.jdbcDriver");
		// dataSource.setUrl("jdbc:hsqldb:hsql://localhost:");
		// dataSource.setUsername("sa");
		// dataSource.setPassword("");
		// return dataSource;
	}

	@Bean
	public PlatformTransactionManager txManager() {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}
}
