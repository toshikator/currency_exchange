package third_project.DbConnection;

import java.sql.*;
import java.util.Properties;

/**
 * Connector/DAO for the exchangerates table using the same database as CurrenciesDB.
 * <p>
 * Expected table structure (Oracle):
 * - BASE_CURRENCY_CODE (VARCHAR2) — code like "USD"
 * - TARGET_CURRENCY_CODE (VARCHAR2) — code like "EUR"
 * - RATE (NUMBER) — rate to convert 1 base to target
 * - (optional) other columns are ignored by SQL below
 * <p>
 * Table name is read from configs/db.properties key: tableNameExchangeRates
 */
public class ExchangeRatesDB {

    static String url;
    static String username;
    static String password;
    static String tableName;

    static {
        // Load database configuration from configs/db.properties on the classpath
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
    public static Double selectRate(String baseCurrencyCode, String targetCurrencyCode) {
        if (baseCurrencyCode == null || targetCurrencyCode == null) return null;
        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        if (baseCurrencyCode.equals(targetCurrencyCode)) return 1.0; // trivial case

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "SELECT RATE FROM " + tableName + " WHERE BASE_CURRENCY_CODE = ? AND TARGET_CURRENCY_CODE = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, baseCurrencyCode);
                    ps.setString(2, targetCurrencyCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            double rate = rs.getDouble("RATE");
                            if (!rs.wasNull()) return rate;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("selectRate exception: " + ex);
            System.err.println(ex);
        }
        return null;
    }

    /**
     * Inserts a new rate or updates existing one for the pair base->target.
     * Returns number of affected rows (1 for insert/update, 0 on failure).
     */
    public static int upsertRate(String baseCurrencyCode, String targetCurrencyCode, double rate) {
        if (baseCurrencyCode == null || targetCurrencyCode == null) return 0;
        baseCurrencyCode = baseCurrencyCode.toUpperCase();
        targetCurrencyCode = targetCurrencyCode.toUpperCase();
        if (rate <= 0) return 0;
        if (baseCurrencyCode.equals(targetCurrencyCode)) return 0;

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                // First try update
                String updateSql = "UPDATE " + tableName + " SET RATE = ? WHERE BASE_CURRENCY_CODE = ? AND TARGET_CURRENCY_CODE = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setDouble(1, rate);
                    ps.setString(2, baseCurrencyCode);
                    ps.setString(3, targetCurrencyCode);
                    int updated = ps.executeUpdate();
                    if (updated > 0) return updated;
                }

                // If not updated, insert
                String insertSql = "INSERT INTO " + tableName + " (BASE_CURRENCY_CODE, TARGET_CURRENCY_CODE, RATE) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, baseCurrencyCode);
                    ps.setString(2, targetCurrencyCode);
                    ps.setDouble(3, rate);
                    return ps.executeUpdate();
                }
            }
        } catch (SQLIntegrityConstraintViolationException dup) {
            // Concurrent insert happened, try update once more
            try {
                Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
                try (Connection conn = DriverManager.getConnection(url, username, password)) {
                    String updateSql = "UPDATE " + tableName + " SET RATE = ? WHERE BASE_CURRENCY_CODE = ? AND TARGET_CURRENCY_CODE = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setDouble(1, rate);
                        ps.setString(2, baseCurrencyCode);
                        ps.setString(3, targetCurrencyCode);
                        return ps.executeUpdate();
                    }
                }
            } catch (Exception nested) {
                System.out.println("upsertRate retry exception: " + nested);
                System.err.println(nested);
            }
        } catch (Exception ex) {
            System.out.println("upsertRate exception: " + ex);
            System.err.println(ex);
        }
        return 0;
    }
}
