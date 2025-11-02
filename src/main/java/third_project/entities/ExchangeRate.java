package third_project.entities;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExchangeRate implements Serializable {
    private static final long serialVersionUID = 1L;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;
    private int id;

    public ExchangeRate() {
    }


    public ExchangeRate(int id, int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;

    }

    public int getBaseCurrencyId() {
        return baseCurrencyId;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        this.baseCurrencyId = baseCurrencyId;
    }

    public int getTargetCurrencyId() {
        return targetCurrencyId;
    }

    public void setTargetCurrencyId(int targetCurrencyId) {
        this.targetCurrencyId = targetCurrencyId;
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
                "baseCurrencyCode='" + baseCurrencyId + '\'' +
                ", targetCurrencyCode='" + targetCurrencyId + '\'' +
                ", rate=" + rate +
                '}';
    }

}
