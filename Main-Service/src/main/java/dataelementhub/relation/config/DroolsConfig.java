package dataelementhub.relation.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieBuilder;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class responsible for initializing the Drools rule engine.
 * The configuration loads the rule sets used for datatype compatibility
 * evaluation and relation classification and exposes the resulting
 * KieContainer as a Spring-managed bean.
 */
@Configuration
public class DroolsConfig {
    /**
     * Creates and configures the Drools KieContainer.
     * During initialization, all required DRL files are loaded from the
     * application resources and compiled into a Drools knowledge base.
     * The resulting KieContainer serves as the entry point for creating
     * rule execution sessions.
     *
     * @return Configured KieContainer instance.
     * @throws RuntimeException if the rule base cannot be created.
     */
    @Bean
    public KieContainer kieContainer() {
        try {
            // Obtain the Drools service factory.
            KieServices ks = KieServices.Factory.get();
            // Create an in-memory file system for rule definitions.
            KieFileSystem kfs = ks.newKieFileSystem();

            // Load relation classification rules.
            kfs.write(ks.getResources().newClassPathResource("rules/relation/relation-suggestion.drl"));

            // Compile all loaded rule files.
            KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
            // Create a KieContainer from the compiled rule module.
            return ks.newKieContainer(kb.getKieModule().getReleaseId());
        } catch (Exception e) {
            // Wrap initialization failures in a runtime exception to prevent
            // application startup with an invalid rule configuration.
            throw new RuntimeException("Failed to create KieContainer", e);
        }
    }
}
