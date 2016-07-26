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

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for Neo4j.
 *
 * @author Stephane Nicoll
 * @author Michael Hunger
 * @author Vince Bickers
 * @since 1.4.0
 */
@ConfigurationProperties(prefix = "spring.data.neo4j")
public class Neo4jProperties implements ApplicationContextAware {

	static final String EMBEDDED_DRIVER = "org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver";

	static final String BOLT_DRIVER = "org.neo4j.ogm.drivers.bolt.driver.BoltDriver";

	static final String HTTP_DRIVER = "org.neo4j.ogm.drivers.http.driver.HttpDriver";

	/**
	 * URI used by the driver.
	 */
	private String uri;

	/**
	 * Login user of the server.
	 */
	private String username;

	/**
	 * Login password of the server.
	 */
	private String password;

	/**
	 * Compiler to use.
	 */
	private String compiler;

	/**
	 * Driver to use.
	 */
	private String driver;

	private ClassLoader classLoader = Neo4jProperties.class.getClassLoader();

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCompiler() {
		return this.compiler;
	}

	public void setCompiler(String compiler) {
		this.compiler = compiler;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.classLoader = ctx.getClassLoader();
	}

	/**
	 * Create a {@link Configuration} based on the state of this instance.
	 *
	 * @return a configuration
	 */
	public Configuration createConfiguration() {
		Configuration configuration = new Configuration();
		DriverConfiguration driverConfiguration = configuration.driverConfiguration();

		if (!StringUtils.isEmpty(this.username) && !StringUtils.isEmpty(this.password)) {
			driverConfiguration.setCredentials(this.username, this.password);
		}

		driverConfiguration.setURI(uri);

		if (uri.startsWith("bolt")) {
			driverConfiguration.setDriverClassName(BOLT_DRIVER);
		} else if (uri.startsWith("http")) {
			driverConfiguration.setDriverClassName(HTTP_DRIVER);
		} else {
			driverConfiguration.setDriverClassName(EMBEDDED_DRIVER);
		}

		if (this.compiler != null) {
			configuration.compilerConfiguration().setCompilerClassName(this.compiler);
		}
		return configuration;
	}
}
