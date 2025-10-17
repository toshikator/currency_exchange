package third_project;

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


    public ExchangeRate(int id, int baseCurrencyCode, int targetCurrencyCode, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
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
        return "ExchangeRate{" +
                "id=" + id +
                "baseCurrencyCode='" + baseCurrencyCode + '\'' +
                ", targetCurrencyCode='" + targetCurrencyCode + '\'' +
                ", rate=" + rate +
                '}';
    }

}
