package io.wegetit.kabod.bankaccount;

import com.opencsv.CSVReader;
import io.wegetit.kabod.common.DataProcessorProperties;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BankTransactionConfig {

    private final static int BATCH_SIZE = 1000;
    private final static DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                List<BankTransactionEntity> batch = new ArrayList<>(BATCH_SIZE);
                while (reader.iterator().hasNext()) {
                    batch.add(convert(source, reader.readNext()));
                    if (batch.size() >= BATCH_SIZE) {
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

    private BankTransactionEntity convert(String source, String[] row) {
        return BankTransactionEntity.builder()
            .id(UUID.randomUUID().toString())
            .source(source)
            .iban(row[0])
            .currency(row[1])
            .date(LocalDateTime.parse(row[2], LOCAL_DATE_TIME_FORMATTER))
            .amount(new BigDecimal(row[3]))
            .build();
    }
}
