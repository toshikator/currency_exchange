package third_project.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.annotation.JsonbProperty;
import third_project.entities.Currency;

import java.math.BigDecimal;

public class DTOExchange {

    @JsonbProperty("amount")
    private final BigDecimal amount;
    @JsonbProperty("convertedAmount")
    private final BigDecimal convertedAmount;
    @JsonbProperty("baseCurrency")
    private final Currency baseCurrency;
    @JsonbProperty("targetCurrency")
    private final Currency targetCurrency;
    @JsonbProperty("rate")
    private final BigDecimal rate;

    public DTOExchange(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {

        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;

    }

    @Override
//    public String toString() {
//        return "{" +
//                "amount=" + amount +
//                ", convertedAmount=" + convertedAmount +
//                ", baseCurrency=" + baseCurrency +
//                ", targetCurrency=" + targetCurrency +
//                ", rate=" + rate +
//                "}";
//    }

    public String toString() {

        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(this);

        return json;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
