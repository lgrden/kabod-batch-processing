package io.wegetit.kabod.feed.bankaccount;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BankTransactionDataGenerator {

    private final List<BankAccount> BANK_ACCOUNTS = List.of(BankAccount.builder().iban("CH9300762011623852957").currency("CHF").build(),
            BankAccount.builder().iban("CH100023000A109822346").currency("CHF").build());

    private final static int DAYS_90_IN_SECONDS = 60*60*24*90;

    public BankTransaction generateBankTransaction() {
        BankAccount bankAccount = BANK_ACCOUNTS.get(ThreadLocalRandom.current().nextInt(BANK_ACCOUNTS.size()));
        return BankTransaction.builder()
            .iban(bankAccount.getIban())
            .currency(bankAccount.getCurrency())
            .date(LocalDateTime.now().minusSeconds(ThreadLocalRandom.current().nextInt(DAYS_90_IN_SECONDS)))
            .amount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(-1000, 1000)).setScale(2, RoundingMode.HALF_UP))
            .build();
    }
}
