package third_project.service;


import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;


public final class Validation {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("com.example");

    public static boolean isStringValid(String str) {

        return str != null && !str.isBlank() && str.matches("[A-Z]{11}");
    }

    public static boolean isStringConvertableToBigDecimal(String str) {
        //        log.info("string on validation" + " " + str);
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean areThreeStringsValid(String name, String code, String sign) {
        return isStringValid(name) && isStringValid(code) && isStringValid(sign);
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

    public static boolean isCurrencyValid(Currency currency) {
        return currency != null && currency.getId() > 0 && currency.getCode() != null && currency.getFullName() != null && currency.getSign() != null;
    }

    public static boolean isCurrencyExist(String currencyCode, CurrenciesDbConnector currenciesDbConnector) throws SQLException {
        return currenciesDbConnector.findByCode(currencyCode) != null;
    }

    public static boolean isValidExchangeRate(ExchangeRate exRate) {
        return exRate != null && exRate.getBaseCurrencyId() > 0 && exRate.getTargetCurrencyId() > 0 &&
                exRate.getRate() != null && exRate.getRate().compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isExchangeRateExist(ExchangeRate exRate) {
        return exRate != null;
    }
}
