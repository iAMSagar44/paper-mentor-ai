package dev.sagar.data_loader.config;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentIntelligenceConfig {

    @Bean
    public DocumentAnalysisClient documentAnalysisClient() {
        return new DocumentAnalysisClientBuilder()
                .endpoint(System.getenv("DOCUMENT_INTELLIGENCE_ENDPOINT"))
                .credential(new AzureKeyCredential(System.getenv("DOCUMENT_INTELLIGENCE_KEY")))
                .buildClient();
    }
}