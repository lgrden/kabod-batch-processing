package io.wegetit.kabod.bankaccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String id;
    private String source;
    private String iban;
    private String currency;
    private LocalDateTime date;
    private BigDecimal amount;
}
