package third_project.entities;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 1L;
    private int baseCurrencyCode;
    private int targetCurrencyCode;
    private BigDecimal rate;
    private int id;

    public ExchangeRate() {
    }


    public ExchangeRate(int id, int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyCode = baseCurrencyId;
        this.targetCurrencyCode = targetCurrencyId;
        this.rate = rate;

    }

    public int getBaseCurrencyCode() {
        return baseCurrencyCode;
    }

    public void setBaseCurrencyCode(int baseCurrencyCode) {
        this.baseCurrencyCode = baseCurrencyCode;
    }

    public int getTargetCurrencyCode() {
        return targetCurrencyCode;
    }

    public void setTargetCurrencyCode(int targetCurrencyCode) {
        this.targetCurrencyCode = targetCurrencyCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                "baseCurrencyCode='" + baseCurrencyCode + '\'' +
                ", targetCurrencyCode='" + targetCurrencyCode + '\'' +
                ", rate=" + rate +
                '}';
    }

}
