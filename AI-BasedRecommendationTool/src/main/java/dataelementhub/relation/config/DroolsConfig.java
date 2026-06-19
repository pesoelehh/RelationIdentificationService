package dataelementhub.relation.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieBuilder;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        try {
            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();

            // Load both DRL files
            kfs.write(ks.getResources().newClassPathResource("rules/datatype/datatype-compatibility.drl"));
            kfs.write(ks.getResources().newClassPathResource("rules/relation/relation-suggestion.drl"));

            KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
            return ks.newKieContainer(kb.getKieModule().getReleaseId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create KieContainer", e);
        }
    }
}
