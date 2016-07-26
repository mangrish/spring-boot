/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.data.neo4j;



import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.ogm.session.SessionFactory;

import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.TestAutoConfigurationPackage;
import org.springframework.boot.autoconfigure.data.neo4j.city.City;
import org.springframework.boot.autoconfigure.data.neo4j.city.CityRepository;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.neo4j.repository.config.EnableExperimentalNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.web.support.OpenSessionInViewFilter;
import org.springframework.data.neo4j.web.support.OpenSessionInViewInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for {@link Neo4jOgmAutoConfiguration}. Tests can't use the embedded driver as we
 * use Lucene 4 and Neo4j still requires 3.
 *
 * @author Stephane Nicoll
 * @author Michael Hunger
 * @author Vince Bickers
 */
public class Neo4jOgmAutoConfigurationTests {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	protected AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@After
	public void close() {
		this.context.close();
	}

	@Test
	public void testSessionCreated() throws Exception {
		setupTestConfiguration();
		this.context.refresh();
		assertThat(this.context.getBean(Neo4jTransactionManager.class)).isNotNull();
	}

	@Test
	public void testOpenSessionInViewInterceptorCreated() throws Exception {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(TestConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class, Neo4jOgmAutoConfiguration.class);
		context.refresh();
		assertThat(context.getBean(OpenSessionInViewInterceptor.class)).isNotNull();
		context.close();
	}


	@Test
	public void testOpenSessionInViewInterceptorNotRegisteredWhenFilterPresent()
			throws Exception {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.register(TestFilterConfiguration.class,
				EmbeddedDataSourceConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class,  Neo4jOgmAutoConfiguration.class);
		context.refresh();
		assertThat(getInterceptorBeans(context).length).isEqualTo(0);
		context.close();
	}

	@Test
	public void testOpenSessionInViewInterceptorNotRegisteredWhenExplicitlyOff()
			throws Exception {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "spring.data.neo4j.open_in_view:false");
		context.register(TestConfiguration.class, EmbeddedDataSourceConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class, Neo4jOgmAutoConfiguration.class);
		context.refresh();
		assertThat(getInterceptorBeans(context).length).isEqualTo(0);
		context.close();
	}

	protected void setupTestConfiguration() {
		setupTestConfiguration(TestConfiguration.class);
	}

	protected void setupTestConfiguration(Class<?> configClass) {
		this.context.register(configClass,
				PropertyPlaceholderAutoConfiguration.class, Neo4jOgmAutoConfiguration.class);
	}

	private String[] getInterceptorBeans(ApplicationContext context) {
		return context.getBeanNamesForType(OpenSessionInViewInterceptor.class);
	}

	@Configuration
	@TestAutoConfigurationPackage(City.class)
	protected static class TestConfiguration {

	}

	@Configuration
	@TestAutoConfigurationPackage(City.class)
	protected static class TestFilterConfiguration {

		@Bean
		public OpenSessionInViewFilter openSessionInViewFilter() {
			return new OpenSessionInViewFilter();
		}
	}

	@Configuration
	protected static class TestConfigurationWithSessionFactory
			extends TestConfiguration {

		@Bean
		public SessionFactory sessionFactory() {
			return new SessionFactory();
		}

		@Bean
		public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
			Neo4jTransactionManager transactionManager = new Neo4jTransactionManager();
			transactionManager.setSessionFactory(sessionFactory);
			return transactionManager;
		}
	}

	@Configuration
	@TestAutoConfigurationPackage(City.class)
	protected static class TestConfigurationWithTransactionManager {

		@Bean
		public PlatformTransactionManager transactionManager() {
			return new CustomNeo4jTransactionManager();
		}
	}

	static class CustomNeo4jTransactionManager extends Neo4jTransactionManager {

	}


	@Configuration
	@EnableExperimentalNeo4jRepositories(basePackageClasses = CityRepository.class)
	@TestAutoConfigurationPackage(City.class)
	static class CustomConfiguration {

	}
}
