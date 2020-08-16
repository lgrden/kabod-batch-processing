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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BankTransactionConfig {

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
    public Tasklet processBankTransactions(DataProcessorProperties bankTransactionProperties, BankTransactionEntityService service) {
        return (stepContribution, chunkContext) -> {
            if (!runBankTransactionJob().test(bankTransactionProperties)) {
                log.info("Nothing to process in {}.",bankTransactionProperties.getSource());
                return RepeatStatus.FINISHED;
            }
            try {
                Files.createDirectories(Paths.get(bankTransactionProperties.getDestination()));
            } catch (IOException e) {
                log.error("Failed to create {} directory.", bankTransactionProperties.getDestination());
                throw e;
            }
            String[] files = Optional.ofNullable(new File(bankTransactionProperties.getSource()).list()).orElse(ArrayUtils.toArray());
            String source = Stream.of(files).filter(p -> StringUtils.startsWith(p ,bankTransactionProperties.getPrefix())).findFirst().orElse(null);
            if (StringUtils.isNotEmpty(source)) {
                log.info("Processing {}.", source);
                File sourceFile = new File(bankTransactionProperties.getSource() + File.separator + source);
                CSVReader reader = new CSVReader(new FileReader(sourceFile));
                List<BankTransactionEntity> transactions = reader.readAll().stream().map(p -> BankTransactionEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .source(source)
                    .iban(p[0])
                    .currency(p[1])
                    .date(LocalDateTime.parse(p[2], LOCAL_DATE_TIME_FORMATTER))
                    .amount(new BigDecimal(p[3]))
                    .build()).collect(Collectors.toList());
                reader.close();
                service.saveAll(transactions);
                log.info("Loaded {} elements.", transactions.size());
                sourceFile.renameTo(new File(bankTransactionProperties.getDestination() + File.separator + source));
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
                    .filter(p -> StringUtils.startsWith(p ,bankTransactionProperties.getPrefix()))
                    .count() > 0;
        };
    }
}
