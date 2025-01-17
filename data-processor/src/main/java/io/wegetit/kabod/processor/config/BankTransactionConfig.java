package io.wegetit.kabod.processor.config;

import com.opencsv.CSVReader;
import io.wegetit.kabod.processor.bankaccount.BankTransactionEntity;
import io.wegetit.kabod.processor.bankaccount.BankTransactionEntityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BankTransactionConfig {

    @Validated
    @Bean
    @ConfigurationProperties(prefix = "processor.transaction")
    public DataProcessorProperties bankTransactionProperties() {
        return new DataProcessorProperties();
    }

    @Bean
    public Job bankTransactionJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, BankTransactionEntityService service) {
        return jobBuilderFactory
            .get("bankTransactionJob")
            .incrementer(new RunIdIncrementer())
            .start(bankTransactionStep(stepBuilderFactory, service))
            .build();
    }

    @Bean
    public Step bankTransactionStep(StepBuilderFactory stepBuilderFactory, BankTransactionEntityService service) {
        return stepBuilderFactory.get("bankTransactionStep")
            .tasklet(processBankTransactions(bankTransactionProperties(), service))
            .build();
    }

    @Bean
    public Tasklet processBankTransactions(@Qualifier("bankTransactionProperties") DataProcessorProperties properties, BankTransactionEntityService service) {
        return (stepContribution, chunkContext) -> {
            if (!runBankTransactionJob().test(properties)) {
                log.info("Nothing to process in {}.",properties.getSource());
                return RepeatStatus.FINISHED;
            }
            try {
                Files.createDirectories(Paths.get(properties.getDestination()));
            } catch (IOException e) {
                log.error("Failed to create {} directory.", properties.getDestination());
                throw e;
            }
            String[] files = Optional.ofNullable(new File(properties.getSource()).list()).orElse(ArrayUtils.toArray());
            String source = Stream.of(files).filter(p -> StringUtils.startsWith(p ,properties.getPrefix())).findFirst().orElse(null);
            if (StringUtils.isNotEmpty(source)) {
                log.info("Processing {}.", source);
                File sourceFile = new File(properties.getSource() + File.separator + source);
                CSVReader reader = new CSVReader(new FileReader(sourceFile));
                AtomicInteger count = new AtomicInteger();
                List<BankTransactionEntity> batch = new ArrayList<>(properties.getBatchSize());
                while (reader.iterator().hasNext()) {
                    String[] data = reader.readNext();
                    if (ArrayUtils.isEmpty(data)) {
                        continue;
                    }
                    batch.add(BankTransactionEntity.of(source, data));
                    if (batch.size() >= properties.getBatchSize()) {
                        service.saveAll(batch);
                        count.addAndGet(batch.size());
                        batch.clear();
                    }
                }
                service.saveAll(batch);
                count.addAndGet(batch.size());
                reader.close();
                log.info("Processed {} elements.", count.get());
                sourceFile.renameTo(new File(properties.getDestination() + File.separator + source));
            }
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Predicate<DataProcessorProperties> runBankTransactionJob() {
        return bankTransactionProperties -> {
            File dir = new File(bankTransactionProperties.getSource());
            return dir.exists() && dir.isDirectory() &&
                Stream.of( Optional.ofNullable(dir.list()).orElse(ArrayUtils.toArray()))
                    .anyMatch(p -> StringUtils.startsWith(p ,bankTransactionProperties.getPrefix()));
        };
    }
}
