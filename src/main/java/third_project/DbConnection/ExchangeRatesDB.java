package third_project.DbConnection;

import third_project.Currency;
import third_project.DTOExchangeRate;
import third_project.ExchangeRate;
import third_project.view.ExchangeRates;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;


public class ExchangeRatesDB {

    static String url;
    static String username;
    static String password;
    static String tableName;

    static int idColumnNumber;
    static int baseCurrencyIdColumnNumber;
    static int targetCurrencyIdColumnNumber;
    static int rateColumnNumber;

    // Strict whitelist for table/identifier names to mitigate SQL injection via config
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    static {
        try {
            Properties props = new Properties();
            try (java.io.InputStream in = ExchangeRatesDB.class.getClassLoader().getResourceAsStream("configs/db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
            String address = props.getProperty("address");
            String port = props.getProperty("port");
            String databaseName = props.getProperty("databaseName");
            username = props.getProperty("username");
            password = props.getProperty("password");
            tableName = props.getProperty("tableNameExchangeRates");
            if (tableName == null || !SAFE_IDENTIFIER.matcher(tableName).matches()) {
                throw new IllegalStateException("Unsafe table name for ExchangeRates: " + tableName);
            }

            idColumnNumber = Integer.parseInt(props.getProperty("exchangeRates.columns.id"));
            baseCurrencyIdColumnNumber = Integer.parseInt(props.getProperty("exchangeRates.columns.baseCurrencyId"));
            targetCurrencyIdColumnNumber = Integer.parseInt(props.getProperty("exchangeRates.columns.targetCurrencyId"));
            rateColumnNumber = Integer.parseInt(props.getProperty("exchangeRates.columns.rate"));

            // Construct Oracle JDBC URL by default using address and databaseName from config
            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
        } catch (Exception e) {
            System.out.println("Failed to load database configuration for ExchangeRatesDB");
            System.err.println(e);
        }
    }

    /**
     * Returns the exchange rate for base->target if present, or null when not found.
     */
    public static ExchangeRate selectRate(int baseCurrencyId, int targetCurrencyId) {
        if (baseCurrencyId == 0 || targetCurrencyId == 0) return null;

        if (baseCurrencyId == targetCurrencyId) return null; // trivial case

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "SELECT * FROM " + tableName + " WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, baseCurrencyId);
                    ps.setInt(2, targetCurrencyId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int id = rs.getInt(idColumnNumber);

                            BigDecimal rate = rs.getBigDecimal(rateColumnNumber);
                            return new ExchangeRate(id, baseCurrencyId, targetCurrencyId, rate);
                        }
                    } catch (SQLException e) {
                        System.out.println("selectRate SQLException: " + e);
                        System.err.println(e);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("selectRate exception: " + ex);
            System.err.println(ex);
        }
        return null;
    }

    public static List<ExchangeRate> selectAll() {

        ArrayList<ExchangeRate> exchangeRates = new ArrayList<ExchangeRate>();
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tableName)) {
                    try (ResultSet resultSet = ps.executeQuery()) {
                        while (resultSet.next()) {
                            int id = resultSet.getInt(idColumnNumber);
                            int baseCurrency = resultSet.getInt(baseCurrencyIdColumnNumber);
                            int targetCurrency = resultSet.getInt(targetCurrencyIdColumnNumber);
                            BigDecimal rate = resultSet.getBigDecimal(rateColumnNumber);
                            ExchangeRate exchangeRate = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                            exchangeRates.add(exchangeRate);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return exchangeRates;
    }

    public static ExchangeRate insert(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Currency baseCurrency = null;
        Currency targetCurrency = null;
        try {
            baseCurrency = CurrenciesDB.selectOne(baseCurrencyCode);
            if (baseCurrency == null) throw new IllegalArgumentException("Invalid baseCurrency code provided");
            targetCurrency = CurrenciesDB.selectOne(targetCurrencyCode);
            if (targetCurrency == null) throw new IllegalArgumentException("Invalid targetCurrency code provided");

        } catch (Exception e) {
            System.out.println("Currency seeking exception on insert: " + e);
            System.err.println(e);
        }
        ExchangeRate exchangeRate = ExchangeRatesDB.insert(baseCurrency.getId(), targetCurrency.getId(), rate);
        return exchangeRate;
    }

    public static ExchangeRate insert(int baseCurrencyCode, int targetCurrencyCode, BigDecimal rate) {
        ExchangeRate exchangeRate = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "INSERT INTO " + tableName + " (BASECURRENCYID, TARGETCURRENCYID, RATE) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, baseCurrencyCode);
                    ps.setInt(2, targetCurrencyCode);
                    ps.setBigDecimal(3, rate);
                    ps.executeUpdate();
                }
            }
            exchangeRate = ExchangeRatesDB.selectRate(CurrenciesDB.selectOne(baseCurrencyCode).getId(), CurrenciesDB.selectOne(targetCurrencyCode).getId());
        } catch (Exception e) {
            System.out.println("insert exception: " + e);
            System.err.println(e);
        }
        return exchangeRate;
    }

    public static ExchangeRate update(int baseCurrencyId, int targetCurrencyId, BigDecimal newRate) {
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "UPDATE " + tableName + " SET RATE = ? WHERE BASECURRENCYID = ? AND TARGETCURRENCYID = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setBigDecimal(1, newRate);
                    ps.setInt(2, baseCurrencyId);
                    ps.setInt(3, targetCurrencyId);
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        return null;
                    }
                }
            }
            return selectRate(baseCurrencyId, targetCurrencyId);
        } catch (Exception e) {
            System.out.println("update exception: " + e);
            System.err.println(e);
            return null;
        }
    }
}
