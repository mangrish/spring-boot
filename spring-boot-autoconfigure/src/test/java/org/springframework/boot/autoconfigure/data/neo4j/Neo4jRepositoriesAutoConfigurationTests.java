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
import org.junit.Test;

import org.neo4j.ogm.session.SessionFactory;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.TestAutoConfigurationPackage;
import org.springframework.boot.autoconfigure.data.alt.neo4j.CityNeo4jRepository;
import org.springframework.boot.autoconfigure.data.empty.EmptyDataPackage;
import org.springframework.boot.autoconfigure.data.neo4j.city.City;
import org.springframework.boot.autoconfigure.data.neo4j.city.CityRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableExperimentalNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Neo4jRepositoriesAutoConfiguration}.
 *
 * @author Dave Syer
 * @author Oliver Gierke
 * @author Michael Hunger
 * @author Vince Bickers
 * @author Stephane Nicoll
 * @author Mark Angrish
 */
public class Neo4jRepositoriesAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		this.context.close();
	}

	@Test
	public void testDefaultRepositoryConfiguration() throws Exception {
		prepareApplicationContext(TestConfiguration.class);

		assertThat(this.context.getBean(CityRepository.class)).isNotNull();
		assertThat(this.context.getBean(PlatformTransactionManager.class)).isNotNull();
		assertThat(this.context.getBean(SessionFactory.class)).isNotNull();
	}

	@Test
	public void testOverrideRepositoryConfiguration() throws Exception {
		prepareApplicationContext(Neo4jOgmAutoConfigurationTests.CustomConfiguration.class);

		assertThat(this.context.getBean(CityRepository.class)).isNotNull();
		assertThat(this.context.getBean(PlatformTransactionManager.class)).isNotNull();
		assertThat(this.context.getBean(SessionFactory.class)).isNotNull();
	}


	@Test
	public void testNoRepositoryConfiguration() throws Exception {
		prepareApplicationContext(EmptyConfiguration.class);
		assertThat(this.context.getBean(SessionFactory.class)).isNotNull();
	}

	@Test
	public void doesNotTriggerDefaultRepositoryDetectionIfCustomized() {
		prepareApplicationContext(CustomizedConfiguration.class);
		assertThat(this.context.getBean(CityNeo4jRepository.class)).isNotNull();
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void autoConfigurationShouldNotKickInEvenIfManualConfigDidNotCreateAnyRepositories() {
		prepareApplicationContext(SortOfInvalidCustomConfiguration.class);

		this.context.getBean(CityRepository.class);
	}

	private void prepareApplicationContext(Class<?>... configurationClasses) {
		this.context = new AnnotationConfigApplicationContext();
		this.context.register(configurationClasses);
		this.context.register(Neo4jOgmAutoConfiguration.class,
				Neo4jRepositoriesAutoConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class);
		this.context.refresh();
	}

	@Configuration
	@TestAutoConfigurationPackage(City.class)
	protected static class TestConfiguration {

	}

	@Configuration
	@TestAutoConfigurationPackage(EmptyDataPackage.class)
	protected static class EmptyConfiguration {

	}


	@Configuration
	@TestAutoConfigurationPackage(City.class)
	@EnableExperimentalNeo4jRepositories(basePackageClasses = CityNeo4jRepository.class)
	protected static class CustomizedConfiguration {

	}

	@Configuration
	// To not find any repositories
	@EnableExperimentalNeo4jRepositories("foo.bar")
	@TestAutoConfigurationPackage(City.class)
	protected static class SortOfInvalidCustomConfiguration {

	}
}
