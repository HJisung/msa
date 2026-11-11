package se.magnus.microservices.core.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class RecommendationServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationServiceApplication.class, args);
    String host = ctx.getEnvironment().getProperty("spring.mongodb.host", "localhost");
    String port = ctx.getEnvironment().getProperty("spring.mongodb.port", "27017");
    LOG.info("MongoDB connection settings: {}:{}", host, port);
	}

}
