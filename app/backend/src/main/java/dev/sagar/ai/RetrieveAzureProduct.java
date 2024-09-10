package dev.sagar.ai;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.springframework.context.annotation.Description;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Description("Azure product names list")
@Service("azureProductNames")
class RetrieveAzureProduct
        implements Function<RetrieveAzureProduct.AzureProduct, RetrieveAzureProduct.AzureProducts> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RetrieveAzureProduct.class);
    private final JdbcClient jdbcClient;

    public RetrieveAzureProduct(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public AzureProducts apply(AzureProduct request) {
        logger.info("Retrieving Azure product names for the service: {} mentioned in the user query",
                request.azureProduct());
        List<String> products = jdbcClient.sql("SELECT product_name FROM azure_services").query(String.class).list();
        return new AzureProducts(products);
    }

    record AzureProduct(String azureProduct) {
    }

    record AzureProducts(List<String> azureProductNames) {
    }
}
