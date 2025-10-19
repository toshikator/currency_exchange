package third_project.DbConnection;

import third_project.Currency;
import third_project.ExchangeRate;
import third_project.view.ExchangeRates;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ExchangeRatesDB {

    static String url;
    static String username;
    static String password;
    static String tableName;

    static int idColumnNumber;
    static int baseCurrencyIdColumnNumber;
    static int targetCurrencyIdColumnNumber;
    static int rateColumnNumber;

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
                    ps.setString(1, Integer.toString(baseCurrencyId));
                    ps.setString(2, Integer.toString(targetCurrencyId));
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

                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
                while (resultSet.next()) {
                    BigDecimal rate = new BigDecimal("1234.567989");
                    int id = resultSet.getInt(idColumnNumber);
                    int baseCurrency = resultSet.getInt(baseCurrencyIdColumnNumber);
                    int targetCurrency = resultSet.getInt(targetCurrencyIdColumnNumber);
                    rate = resultSet.getBigDecimal(rateColumnNumber);
                    ExchangeRate exchangeRate = new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                    exchangeRates.add(exchangeRate);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return exchangeRates;
    }
}
