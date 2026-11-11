package se.magnus.microservices.core.product;
import org.springframework.context.annotation.ComponentScan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ComponentScan("se.magnus")
public class ProductServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ProductServiceApplication.class, args);
    String host = ctx.getEnvironment().getProperty("spring.mongodb.host", "localhost");
    String port = ctx.getEnvironment().getProperty("spring.mongodb.port", "27017");
    LOG.info("MongoDB connection settings: {}:{}", host, port);
	}

}
