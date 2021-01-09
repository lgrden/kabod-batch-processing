package io.wegetit.kabod.processor.datafeed.bankaccount;

import io.wegetit.kabod.processor.datafeed.common.DataFeedProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration

public class BankTransactionConfig {

    @Validated
    @Bean
    @ConfigurationProperties(prefix = "feed.transaction")
    public DataFeedProperties bankTransactionProperties() {
        return new DataFeedProperties();
    }

}
