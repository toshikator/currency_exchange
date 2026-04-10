package third_project.DbConnection;


import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.PropertiesReader;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;


public class ExchangeRatesDbConnector {

    private static final Logger log = Logger.getLogger("com.example");
    private final CurrenciesDbConnector currenciesDbConnector;
    PropertiesReader pr;

    public ExchangeRatesDbConnector(PropertiesReader pr, CurrenciesDbConnector currenciesDbConnector) {
        this.pr = pr;
        this.currenciesDbConnector = currenciesDbConnector;
    }


    public Optional<ExchangeRate> findRate(int baseCurrencyId, int targetCurrencyId) throws SQLException {
        if (baseCurrencyId == 0 || targetCurrencyId == 0 || baseCurrencyId == targetCurrencyId) return Optional.empty();

        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "SELECT * FROM " + pr.getExchangeRatesTableName() + " WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, baseCurrencyId);
                ps.setInt(2, targetCurrencyId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(pr.getExchangeRatesIdCol());

                        BigDecimal rate = rs.getBigDecimal(pr.getRateCol());
                        return Optional.ofNullable(new ExchangeRate(id, baseCurrencyId, targetCurrencyId, rate));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public Optional<ExchangeRate> update(int baseCurrencyId, int targetCurrencyId, BigDecimal newRate) throws SQLException {

        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "UPDATE " + pr.getExchangeRatesTableName() + " SET RATE = ? WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBigDecimal(1, newRate);
                ps.setInt(2, baseCurrencyId);
                ps.setInt(3, targetCurrencyId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    return Optional.empty();
                }

            }
            return findRate(baseCurrencyId, targetCurrencyId);
        }
    }

    public List<ExchangeRate> selectAll() throws SQLException {

        ArrayList<ExchangeRate> exchangeRates = new ArrayList<>();

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
        }
        return exchangeRates;
    }

    public Optional<ExchangeRate> insert(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException {

        Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode).orElseThrow(() -> new IllegalArgumentException("Invalid baseCurrency code provided"));
        Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode).orElseThrow(() -> new IllegalArgumentException("Invalid targetCurrency code provided"));

        return this.insert(baseCurrency.getId(), targetCurrency.getId(), rate);
    }

    public Optional<ExchangeRate> insert(int baseCurrencyCode, int targetCurrencyCode, BigDecimal rate) throws SQLException {

        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "INSERT INTO " + pr.getExchangeRatesTableName() + " (BASECURRENCYID, TARGETCURRENCYID, RATE) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, baseCurrencyCode);
                ps.setInt(2, targetCurrencyCode);
                ps.setBigDecimal(3, rate);
                ps.executeUpdate();
            }
            Currency baseCurrency = currenciesDbConnector.findById(baseCurrencyCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or missed baseCurrency code provided"));
            Currency targetCurrency = currenciesDbConnector.findById(targetCurrencyCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or missed targetCurrency code provided"));

            return this.findRate(baseCurrency.getId(), targetCurrency.getId());
        }
    }
}
