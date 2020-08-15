package io.wegetit.kabod.datafeed.bankaccount;

import com.opencsv.CSVWriter;
import io.wegetit.kabod.datafeed.common.DataFeedProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@AllArgsConstructor
@Service
@Slf4j
public class BankTransactionFeed {

    @Qualifier("bankTransactionProperties")
    private final DataFeedProperties properties;

    private BankTransactionDataGenerator generator;

    @PostConstruct
    private void init() {
        log.info("Creating feed using {} properties.", properties);
    }

    @Scheduled(initialDelayString = "#{@bankTransactionProperties.initialDelay}",
        fixedDelayString = "#{@bankTransactionProperties.fixedDelay}")
    public void process() throws IOException{
        long start = System.currentTimeMillis();
        try {
            Files.createDirectories(Paths.get(properties.getDestination()));
        } catch (IOException e) {
            log.error("Failed to create {} directory.", properties.getDestination());
            throw e;
        }
        long count = 0;
        for (int i = 0; i < properties.getFileCount() ; i++) {
            File file = new File(properties.getDestination() + "/" + properties.getPrefix() + "_" + i + "_" + System.currentTimeMillis());
            try {
                count += writeToFile(file);
            } catch (Exception e) {
                log.error("Failed to write csv file {}.", file, e);
                throw e;
            }
        }
        long end = System.currentTimeMillis();
        log.error("Created {} files with total {} elements in {} ms.", properties.getFileCount(), count, (end - start));
    }

    private int writeToFile(File file) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(file, true));
        List<BankTransaction> list = generateBankTransactions();
        list.forEach(p -> writer.writeNext(p.toStringArray()));
        writer.close();
        log.info("Generated {} elements in {}.", list.size(), file);
        return list.size();
    }

    private List<BankTransaction> generateBankTransactions() {
        int size = ThreadLocalRandom.current().nextInt(properties.getMinRows(), properties.getMaxRows());
        return IntStream.rangeClosed(0, size).mapToObj(p -> generator.generateBankTransaction()).collect(Collectors.toList());
    }
}
