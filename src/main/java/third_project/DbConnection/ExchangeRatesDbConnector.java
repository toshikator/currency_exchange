package third_project.DbConnection;


import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.PropertiesReader;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.logging.Logger;


public class ExchangeRatesDbConnector {

    private static final Logger log = Logger.getLogger("com.example");
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private final CurrenciesDbConnector currenciesDbConnector;
    PropertiesReader pr;

    public ExchangeRatesDbConnector(PropertiesReader pr, CurrenciesDbConnector currenciesDbConnector) {
        this.pr = pr;
        this.currenciesDbConnector = currenciesDbConnector;
    }


    public ExchangeRate findRate(int baseCurrencyId, int targetCurrencyId) {
        if (baseCurrencyId == 0 || targetCurrencyId == 0) return null;

        if (baseCurrencyId == targetCurrencyId) return null;


        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "SELECT * FROM " + pr.getExchangeRatesTableName() + " WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, baseCurrencyId);
                ps.setInt(2, targetCurrencyId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(pr.getExchangeRatesIdCol());

                        BigDecimal rate = rs.getBigDecimal(pr.getRateCol());
                        return new ExchangeRate(id, baseCurrencyId, targetCurrencyId, rate);
                    }
                } catch (SQLException e) {
                    log.info("selectRate SQLException: " + e + " [File: ExchangeRatesDbConnector.java]");
                }
            }

        } catch (Exception ex) {
            log.info("selectRate exception: " + ex + " [File: ExchangeRatesDbConnector.java]");
        }
        return null;
    }

    public ExchangeRate update(int baseCurrencyId, int targetCurrencyId, BigDecimal newRate) {

        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "UPDATE " + pr.getExchangeRatesTableName() + " SET RATE = ? WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, newRate);
                ps.setInt(2, baseCurrencyId);
                ps.setInt(3, targetCurrencyId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    return null;
                }

            }
            return findRate(baseCurrencyId, targetCurrencyId);
        } catch (Exception e) {
            log.info("update exception: " + e + " [File: ExchangeRatesDbConnector.java]");
            return null;
        }
    }

    public List<ExchangeRate> selectAll() {

        ArrayList<ExchangeRate> exchangeRates = new ArrayList<ExchangeRate>();

        try (Connection conn = DBSource.get().getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + pr.getExchangeRatesTableName())) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt(pr.getExchangeRatesIdCol());
                        int baseCurrency = resultSet.getInt(pr.getBaseCurrencyIdCol());
                        int targetCurrency = resultSet.getInt(pr.getTargetCurrencyIdCol());
                        BigDecimal rate = resultSet.getBigDecimal(pr.getRateCol());
                        ExchangeRate exchangeRate = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                        exchangeRates.add(exchangeRate);
                    }

                }
            }
        } catch (Exception ex) {
            log.info(String.valueOf(ex) + " [File: ExchangeRatesDbConnector.java]");
        }
        return exchangeRates;
    }

    public ExchangeRate insert(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Currency baseCurrency = null;
        Currency targetCurrency = null;
        try {
            baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            if (baseCurrency == null) throw new IllegalArgumentException("Invalid baseCurrency code provided");
            targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);
            if (targetCurrency == null) throw new IllegalArgumentException("Invalid targetCurrency code provided");

        } catch (Exception e) {
            log.info("Currency seeking exception on insert: " + e + " [File: ExchangeRatesDbConnector.java]");
        }
        ExchangeRate exchangeRate = this.insert(baseCurrency.getId(), targetCurrency.getId(), rate);
        return exchangeRate;
    }

    public ExchangeRate insert(int baseCurrencyCode, int targetCurrencyCode, BigDecimal rate) {
        ExchangeRate exchangeRate = null;

        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "INSERT INTO " + pr.getExchangeRatesTableName() + " (BASECURRENCYID, TARGETCURRENCYID, RATE) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, baseCurrencyCode);
                ps.setInt(2, targetCurrencyCode);
                ps.setBigDecimal(3, rate);
                ps.executeUpdate();
            }

            exchangeRate = this.findRate(currenciesDbConnector.findById(baseCurrencyCode).getId(), currenciesDbConnector.findById(targetCurrencyCode).getId());
        } catch (Exception e) {
            log.info("insert exception: " + e + " [File: ExchangeRatesDbConnector.java]");
        }
        return exchangeRate;
    }
}
