package io.wegetit.kabod.processor.bankaccount;

import io.wegetit.kabod.processor.common.DataProcessorProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

@AllArgsConstructor
@Service
@Slf4j
public class BankTransactionProcesor {

    private final static DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Qualifier("bankTransactionProperties")
    private final DataProcessorProperties properties;

    private final JobLauncher jobLauncher;
    private final Job bankTransactionJob;
    private final Predicate<DataProcessorProperties> runBankTransactionJob;

    @PostConstruct
    private void init() {
        log.info("Creating processor using {} properties.", properties);
    }

    @Scheduled(initialDelayString = "#{@bankTransactionProperties.initialDelay}",
        fixedDelayString = "#{@bankTransactionProperties.fixedDelay}")
    public void process() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        if (!runBankTransactionJob.test(properties)) {
            return;
        }
        JobParameters params = new JobParametersBuilder()
            .addString("JobTimestamp", String.valueOf(System.currentTimeMillis()))
            .toJobParameters();

        jobLauncher.run(bankTransactionJob, params);
    }
}
