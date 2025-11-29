package third_project.dto;

import third_project.entities.Currency;

import java.math.BigDecimal;

public class DTOExchange {
    private final BigDecimal amount;
    private final BigDecimal convertedAmount;
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private final BigDecimal rate;

    public DTOExchange(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {

        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;

    }

    @Override
    public String toString() {
        return "{" +
                "amount=" + amount +
                ", convertedAmount=" + convertedAmount +
                ", baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                '}';
    }

}
