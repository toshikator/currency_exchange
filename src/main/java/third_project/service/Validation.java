package third_project.service;


import third_project.DbConnection.CurrenciesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.math.BigDecimal;
import java.sql.SQLException;


public final class Validation {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("com.example");

    public static boolean isStringValid(String str) {
        String regex = "^(?!.*--)[A-Za-z0-9 !@#$%^&()_+=\\[\\]{}:,.?|-]{1,28}$";
        return str != null && !str.isBlank() && str.matches(regex);
    }

    public static boolean isStringValidForCurrencySign(String str) {
        String regex = "^(?!.*--)[A-Za-z0-9 !@#$%^&()_+=\\[\\]{}:,.?|-]{1,3}$";
        return str != null && !str.isBlank() && str.matches(regex);
    }

    public static boolean isStringValidForCurrencyCode(String str) {
        String regex = "[A-Za-z]{3}";
        return str != null && !str.isBlank() && str.matches(regex);
    }


    public static boolean isStringConvertableToBigDecimalRate(String str) {
        if (!isStringValid(str)) return false;
        try {
            new BigDecimal(str);
            return !isZeroOrNegative(new BigDecimal(str));
        } catch (NumberFormatException e) {
            log.warning("string on validation" + " " + str + " " + e.getMessage() + " [File: Validation.java]");
            return false;
        }
    }


    public static boolean areThreeStringsValid(String name, String code, String sign) {
        return isStringValid(name) && isStringValid(code) && isStringValid(sign) && code.length() == 3 && sign.length() == 3;
    }

    public static boolean isZeroOrNegative(BigDecimal var) {
        return var.compareTo(BigDecimal.ZERO) <= 0;
    }

    public static boolean isPatchRequestValid(String requestBody) {
        if (requestBody == null || requestBody.trim().length() != 7 || !isStringValid(requestBody.substring(1)))
            return false;
        String firstCode = requestBody.trim().substring(1, 4).toUpperCase();
        String secondCode = requestBody.trim().substring(4, 7).toUpperCase();
        return !firstCode.equals(secondCode);
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String requireCurrencyCode(String code) {

        if (isBlank(code)) {
            throw new IllegalArgumentException("Currency code is required");
        }

        String normalized = code.trim().toUpperCase();

        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must contain exactly 3 Latin letters");
        }
        return normalized;
    }

    public static String requireCurrencySign(String sign) {

        if (isBlank(sign)) {
            throw new IllegalArgumentException("Currency code is required");
        }

        String normalized = sign.trim().toUpperCase();

        if (!normalized.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must contain exactly 3 Latin letters");
        }
        return normalized;
    }

    public static boolean isCurrencyValid(Currency currency) {

        return currency != null && currency.getId() > 0 && currency.getCode() != null && currency.getName() != null && currency.getSign() != null;
    }

    public static boolean isCurrencyExist(String currencyCode, CurrenciesDbConnector currenciesDbConnector) throws SQLException {
        return currenciesDbConnector.findByCode(currencyCode).isPresent();
    }

    public static boolean isValidExchangeRate(ExchangeRate exRate) {
        return exRate != null && exRate.getBaseCurrencyId() > 0 && exRate.getTargetCurrencyId() > 0 &&
                exRate.getRate() != null && exRate.getRate().compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isExchangeRateExist(ExchangeRate exRate) {
        return exRate != null;
    }
}
