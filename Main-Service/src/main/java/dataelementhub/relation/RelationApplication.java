package dataelementhub.relation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class of the Relation Service.
 *
 * This class serves as the entry point for the Spring Boot application
 * developed as part of the master's thesis project. The service provides
 * functionality for semantic relation analysis and similarity evaluation
 * between data elements through REST-based interfaces.
 *
 * The @SpringBootApplication annotation combines:
 * - @Configuration
 * - @EnableAutoConfiguration
 * - @ComponentScan
 *
 * During startup, Spring Boot initializes the application context,
 * loads all configured beans, and exposes the available REST endpoints.
 */
@SpringBootApplication
public class RelationApplication {
    /**
     * Starts the Relation Service application.
     *
     * This method initializes the Spring Boot runtime environment and
     * launches the embedded web server. After startup, all application
     * components such as controllers, services, repositories, and
     * configuration classes become available within the Spring context.
     *
     * @param args command-line arguments provided during application startup
     */

    public static void main(String[] args) {
        SpringApplication.run(RelationApplication.class, args);
    }

}
