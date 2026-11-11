package se.magnus.microservices.core.review;

import java.time.Duration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;

public abstract class MySqlTestBase {
    @ServiceConnection
    private static final MySQLContainer database =
            new MySQLContainer("mysql:9.2.0").withStartupTimeout(Duration.ofSeconds(300));

    static {
        database.start();
    }
}