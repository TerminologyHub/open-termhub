package com.wci.termhub.open.configuration;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Properties configuration.
 */
@Configuration
@EnableAutoConfiguration
public class PropertiesConfiguration {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(PropertiesConfiguration.class);

	/** The env. */
	@Autowired
	private Environment env;

	/**
	 * Instantiates an empty {@link PropertiesConfiguration}.
	 */
	public PropertiesConfiguration() {
		logger.info("CREATE PropertiesConfiguration");
	}

	/**
	 * Returns the properties.
	 *
	 * @return the properties
	 */
	@SuppressWarnings({ "rawtypes" })
	@ConfigurationProperties
	@Bean
	public ApplicationProperties applicationProperties() {
		logger.info("CREATE ApplicationProperties");
		final ApplicationProperties prop = new ApplicationProperties();
		for (final Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext();) {
			final PropertySource propertySource = (PropertySource) it.next();
			if (propertySource instanceof MapPropertySource) {
				for (final Map.Entry<String, Object> entry : ((MapPropertySource) propertySource).getSource()
						.entrySet()) {
					prop.setProperty(entry.getKey(), entry.getValue().toString());
				}
			}
		}

		logger.info("Loaded properties: {}", prop);
		return prop;
	}

}
