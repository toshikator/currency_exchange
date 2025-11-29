package third_project.service;


import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;


public final class Validation {

    private static CurrenciesDbConnector currenciesDbConnector;

    private static ExchangeRatesDbConnector exchangeRatesDbConnector;

    public static boolean isStringValid(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean isStringConvertableToBigDecimal(String str) throws IllegalArgumentException {
        try {
            BigDecimal bd = new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format");
        }
    }

    public static boolean isZeroOrNegative(BigDecimal var) {
        return var.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static boolean isPatchRequestValid(String requestBody) {
        if (requestBody == null || requestBody.length() < 7) return false;
        String firstCode = requestBody.substring(1, 4).toUpperCase();
        String secondCode = requestBody.substring(4, 7).toUpperCase();
        if (firstCode.equals(secondCode)) return false;
        return isValidCurrencyCode(firstCode) && isValidCurrencyCode(secondCode);
    }

    public static boolean isValidCurrencyCode(String code) {
        return code != null && code.length() == 3 && code.matches("[A-Z]{3}");
    }

    public static boolean isValidCurrency(Currency currency) {
        return currency != null && currency.getId() > 0 && currency.getCode() != null && currency.getFullName() != null && currency.getSign() != null;
    }

//    public static boolean isCurrencyExist(Currency currency) {
//        return isValidCurrency(currency) && currenciesDbConnector.findById(currency.getId()) != null;
//    }

    public static boolean isCurrencyExist(String currencyCode) throws SQLException {
        return currenciesDbConnector.findByCode(currencyCode) != null;
    }

    public static boolean isValidExchangeRate(ExchangeRate exRate) {
        return isValidCurrency(currenciesDbConnector.findById(exRate.getBaseCurrencyId())) &&
                isValidCurrency(currenciesDbConnector.findById(exRate.getTargetCurrencyId())) &&
                exRate.getRate() != null && exRate.getRate().compareTo(BigDecimal.ZERO) > 0;
    }
}
