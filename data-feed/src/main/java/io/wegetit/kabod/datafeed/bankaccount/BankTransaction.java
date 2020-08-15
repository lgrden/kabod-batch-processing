package io.wegetit.kabod.datafeed.bankaccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BankTransaction {

    private final static DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String iban;
    private String currency;
    private LocalDateTime date;
    private BigDecimal amount;

    public String[] toStringArray() {
        return new String[] {getIban(), getCurrency(), LOCAL_DATE_TIME_FORMATTER.format(getDate()), getAmount().toString()};
    }
}
