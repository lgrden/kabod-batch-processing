package io.wegetit.kabod.processor.bankaccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import lombok.*;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@AllArgsConstructor
@Document(collection = "bankTransactions")
@TypeAlias("bankTransaction")
@EqualsAndHashCode(of = "id")
@ToString
public class BankTransactionEntity {

    private final static DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String id;
    private String source;
    private String iban;
    private String currency;
    private LocalDateTime date;
    private BigDecimal amount;

    public static BankTransactionEntity of(String source, String[] row) {
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
