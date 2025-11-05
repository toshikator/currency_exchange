package third_project.service;

import third_project.DbConnection.CurrenciesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;


public final class Validation {
    CurrenciesDbConnector currenciesDbConnector;

    private Validation() {
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
    }

    public static boolean isValidCurrencyCode(String code) {
        return code != null && code.length() == 3 && code.matches("[A-Z]{3}");
    }

    public static boolean isValidCurrency(Currency currency) {
        return currency != null && currency.getId() > 0 && currency.getCode() != null && currency.getFullName() != null && currency.getSign() != null;
    }

    public static boolean isCurrencyExist(Currency currency) {
        return isValidCurrency(currency) && CurrenciesDbConnector.findById(currency.getId()) != null;
    }

    public static boolean isCurrencyExist(String currencyCode) throws SQLException {
        return CurrenciesDbConnector.findByCode(currencyCode) != null;
    }

    public static boolean isValidExchangeRate(ExchangeRate exRate) {
        return isValidCurrency(CurrenciesDbConnector.findById(exRate.getBaseCurrencyId())) &&
                isValidCurrency(CurrenciesDbConnector.findById(exRate.getTargetCurrencyId())) &&
                exRate.getRate() != null && exRate.getRate().compareTo(BigDecimal.ZERO) > 0;
    }


}
