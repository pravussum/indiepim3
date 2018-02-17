package net.mortalsilence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@SpringBootApplication
public class IndiepimApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(IndiepimApplication.class, args);
	}

	@Bean
	public SessionRegistry getSessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(IndiepimApplication.class);
	}
}
