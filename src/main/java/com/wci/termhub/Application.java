package com.wci.termhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * The Class Application.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan
public class Application {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(final String[] args) throws Exception {

		SpringApplication.run(Application.class, args);
	}
}
