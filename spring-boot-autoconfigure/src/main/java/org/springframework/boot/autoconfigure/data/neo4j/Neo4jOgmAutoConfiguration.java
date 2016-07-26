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

import java.util.List;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.web.support.OpenSessionInViewFilter;
import org.springframework.data.neo4j.web.support.OpenSessionInViewInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Data Neo4j.
 *
 * @author Mark Angrish
 * @since 1.4.0
 */
@Configuration
@ConditionalOnClass({SessionFactory.class, EnableTransactionManagement.class, Session.class})
@EnableConfigurationProperties(Neo4jProperties.class)
public class Neo4jOgmAutoConfiguration implements BeanFactoryAware {

	private final Neo4jProperties properties;

	private ConfigurableListableBeanFactory beanFactory;

	public Neo4jOgmAutoConfiguration(Neo4jProperties properties) {
		this.properties = properties;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Bean
	@ConditionalOnMissingBean(PlatformTransactionManager.class)
	public PlatformTransactionManager transactionManager() {
		return new Neo4jTransactionManager();
	}

	@Bean
	@Primary
	@ConditionalOnMissingBean(SessionFactory.class)
	public SessionFactory sessionFactory() {
		return new SessionFactory(getPackagesToScan());
	}

	protected String[] getPackagesToScan() {
		List<String> packages = EntityScanPackages.get(this.beanFactory)
				.getPackageNames();
		if (packages.isEmpty() && AutoConfigurationPackages.has(this.beanFactory)) {
			packages = AutoConfigurationPackages.get(this.beanFactory);
		}
		return packages.toArray(new String[packages.size()]);
	}

	protected final Neo4jProperties getProperties() {
		return this.properties;
	}

	@Configuration
	@ConditionalOnWebApplication
	@ConditionalOnClass(WebMvcConfigurerAdapter.class)
	@ConditionalOnMissingBean({OpenSessionInViewInterceptor.class,
			OpenSessionInViewFilter.class})
	@ConditionalOnProperty(prefix = "spring.data.neo4j", name = "open-in-view", havingValue = "true", matchIfMissing = true)
	protected static class Neo4jWebConfiguration {

		// Defined as a nested config to ensure WebMvcConfigurerAdapter is not read when
		// not on the classpath
		@Configuration
		protected static class Neo4jWebMvcConfiguration extends WebMvcConfigurerAdapter {

			@Bean
			public OpenSessionInViewInterceptor openSessionInViewInterceptor() {
				return new OpenSessionInViewInterceptor();
			}

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addWebRequestInterceptor(openSessionInViewInterceptor());
			}
		}
	}
}
