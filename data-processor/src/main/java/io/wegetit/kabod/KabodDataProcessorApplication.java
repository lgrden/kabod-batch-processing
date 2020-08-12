package io.wegetit.kabod;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing
@PropertySource("classpath:application.properties")
public class KabodDataProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(KabodDataProcessorApplication.class, args);
    }
}
