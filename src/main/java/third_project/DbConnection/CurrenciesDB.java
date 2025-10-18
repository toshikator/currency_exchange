package third_project.DbConnection;

import third_project.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

public class CurrenciesDB {


    // Column indices are loaded from configs/db.properties at class initialization
    static int idColumnNumber;
    static int codeColumnNumber;
    static int fullNameColumnNumber;
    static int signColumnNumber;

    static String url;
    static String username;
    static String password;
    static String tableName;

    static {
        // Load database configuration from configs/db.properties on the classpath
        try {
            Properties props = new Properties();
            try (java.io.InputStream in = CurrenciesDB.class.getClassLoader().getResourceAsStream("configs/db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
            String address = props.getProperty("address");
            String port = props.getProperty("port");
            String databaseName = props.getProperty("databaseName");
            username = props.getProperty("username");
            password = props.getProperty("password");
            tableName = props.getProperty("tableNameCurrencies");

            idColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.id"));
            codeColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.code"));
            fullNameColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.fullName"));
            signColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.sign"));


            // Construct Oracle JDBC URL by default using address and databaseName from config
            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
        } catch (Exception e) {
            System.out.println("Failed to load database configuration");
            System.err.println(e);
        }
    }

    public static ArrayList<Currency> select() {


        ArrayList<Currency> currencies = new ArrayList<Currency>();
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
                while (resultSet.next()) {

                    int id = resultSet.getInt(idColumnNumber);
                    String code = resultSet.getString(codeColumnNumber);
                    String fullName = resultSet.getString(fullNameColumnNumber);
                    String sign = resultSet.getString(signColumnNumber);
                    Currency currency = new Currency(id, code, fullName, sign);
                    currencies.add(currency);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return currencies;
    }

    public static Currency selectOne(int id) {
        Currency currency = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setInt(1, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String code = resultSet.getString(codeColumnNumber);
                        String fullName = resultSet.getString(fullNameColumnNumber);
                        String sign = resultSet.getString(signColumnNumber);
                        currency = new Currency(id, code, fullName, sign);

                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception in selectOne by Currency id");
            System.out.println("Currency id = " + id);
            System.out.println(ex);
        }
        return currency;
    }

    public static Currency selectOne(String code) throws SQLException {
        Currency currency = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String sql = "SELECT * FROM " + tableName + " WHERE CODE = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, code.toUpperCase());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        int id = resultSet.getInt(idColumnNumber);
                        String fullName = resultSet.getString(fullNameColumnNumber);
                        String sign = resultSet.getString(signColumnNumber);
                        currency = new Currency(id, code.toUpperCase(), fullName, sign);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception in selectOne by Currency Code");
            System.out.println("Currency code = " + code);
            System.out.println(ex);
            throw new SQLException("Currency not found in the database");
        }
        return currency;
    }

    public static Currency insert(Currency currency) {

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                String sql = "INSERT INTO " + tableName + " (CODE, SIGN, FULL_NAME) Values (?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, currency.getCode().toUpperCase());
                    preparedStatement.setString(2, currency.getSign());
                    preparedStatement.setString(3, currency.getFullName());
                    preparedStatement.executeUpdate();
                    return selectOne(currency.getCode());
                }
            }
        } catch (Exception ex) {
            System.out.println("insert exception: " + ex);
            System.out.println(ex);
        }
        return null;
    }

    public static int update(Currency currency) {

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                String sql = "UPDATE " + tableName + " SET CODE = ?, SIGN = ?, FULL_NAME = ? WHERE id = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, currency.getCode().toUpperCase());
                    preparedStatement.setString(2, currency.getSign());
                    preparedStatement.setString(3, currency.getFullName());
                    preparedStatement.setInt(4, currency.getId());

                    return preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
            System.out.println("update exception: " + ex);
            System.out.println(ex);
        }
        return 0;
    }

    public static int delete(int id) {

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                String sql = "DELETE FROM " + tableName + " WHERE id = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setInt(1, id);

                    return preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
            System.out.println("delete exception: " + ex);
            System.out.println(ex);
        }
        return 0;
    }
}
