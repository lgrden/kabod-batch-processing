package io.wegetit.kabod.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@PropertySource("classpath:application.properties")
public class KabodDataFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(KabodDataFeedApplication.class, args);
    }
}
