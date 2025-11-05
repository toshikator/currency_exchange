package third_project.service;


import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;


public final class Validation {

    private CurrenciesDbConnector currenciesDbConnector;

    private ExchangeRatesDbConnector exchangeRatesDbConnector;

    public Validation() {

//        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");


    }

    public boolean isValidCurrencyCode(String code) {
        return code != null && code.length() == 3 && code.matches("[A-Z]{3}");
    }

    public boolean isValidCurrency(Currency currency) {
        return currency != null && currency.getId() > 0 && currency.getCode() != null && currency.getFullName() != null && currency.getSign() != null;
    }

    public boolean isCurrencyExist(Currency currency) {
        return isValidCurrency(currency) && currenciesDbConnector.findById(currency.getId()) != null;
    }

    public boolean isCurrencyExist(String currencyCode) throws SQLException {
        return currenciesDbConnector.findByCode(currencyCode) != null;
    }

    public boolean isValidExchangeRate(ExchangeRate exRate) {
        return isValidCurrency(currenciesDbConnector.findById(exRate.getBaseCurrencyId())) &&
                isValidCurrency(currenciesDbConnector.findById(exRate.getTargetCurrencyId())) &&
                exRate.getRate() != null && exRate.getRate().compareTo(BigDecimal.ZERO) > 0;
    }


}
