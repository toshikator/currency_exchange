package third_project.dto;

import third_project.DbConnection.CurrenciesDbConnector;
import third_project.entities.Currency;

import java.math.BigDecimal;

public class DTOExchangeRate implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;

    public DTOExchangeRate() {
    }

    public DTOExchangeRate(int id, Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        try {
            this.baseCurrency = baseCurrency;
            if (this.baseCurrency == null) throw new IllegalArgumentException("Invalid baseCurrency ID provided");
            this.targetCurrency = targetCurrency;
            if (this.targetCurrency == null) throw new IllegalArgumentException("Invalid targetCurrency ID provided");

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency ID provided", e);
        }
        this.rate = rate;
    }


    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", baseCurrency=" + baseCurrency +
                ", targetCurrency=" + targetCurrency +
                ", rate=" + rate +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
