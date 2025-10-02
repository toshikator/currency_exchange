package third_project;

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
            String address = props.getProperty("address", "10.100.102.14");
            String port = props.getProperty("port", "1521");
            String databaseName = props.getProperty("databaseName", "XEPDB1");
            username = props.getProperty("username", "hr");
            password = props.getProperty("password", "hr");
            tableName = props.getProperty("tableNameCurrencies", "employees");
            // Load column numbers (1-based) from config with sensible defaults
            try {
                idColumnNumber = Integer.parseInt(props.getProperty("columns.id" ));
            } catch (NumberFormatException ignore) { idColumnNumber = 1; }
            try {
                codeColumnNumber = Integer.parseInt(props.getProperty("columns.code"));
            } catch (NumberFormatException ignore) { codeColumnNumber = 2; }
            try {
                fullNameColumnNumber = Integer.parseInt(props.getProperty("columns.fullName"));
            } catch (NumberFormatException ignore) { fullNameColumnNumber = 3; }
            try {
                signColumnNumber = Integer.parseInt(props.getProperty("columns.sign"));
            } catch (NumberFormatException ignore) { signColumnNumber = 4; }

            // Construct Oracle JDBC URL by default using address and databaseName from config
            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
        } catch (Exception e) {
            // Fallback to previous hardcoded values if anything goes wrong
            url = "jdbc:oracle:thin:@//10.100.102.14:1521/XEPDB1";
            username = "hr";
            password = "hr";
            tableName = "employees";
            idColumnNumber = 1;
            codeColumnNumber = 2;
            fullNameColumnNumber = 3;
            signColumnNumber = 4;
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

                String sql = "SELECT * FROM " + tableName + " WHERE id=" + id;
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setInt(idColumnNumber, id);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {


                        String code = resultSet.getString(codeColumnNumber);
                        String fullName = resultSet.getString(fullNameColumnNumber);
                        String sign = resultSet.getString(signColumnNumber);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return currency;
    }

    public static int insert(Currency currency) {

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                String sql = "INSERT INTO " + tableName + " (name, price) Values (?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(fullNameColumnNumber, currency.getFullName());
                    preparedStatement.setString(codeColumnNumber, currency.getCode());
                    preparedStatement.setString(signColumnNumber, currency.getSign());

//                    preparedStatement.setInt(salaryColumnNumber, currency.getSalary());

                    return preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return 0;
    }

    public static int update(Currency currency) {

        try {
            Class.forName("oracle.jdbc.OracleDriver").getDeclaredConstructor().newInstance();
            try (Connection conn = DriverManager.getConnection(url, username, password)) {

                String sql = "UPDATE " + tableName + " SET name = ?, price = ? WHERE id = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(fullNameColumnNumber, currency.getFullName());
                    preparedStatement.setString(codeColumnNumber, currency.getCode());
                    preparedStatement.setString(signColumnNumber, currency.getSign());
//                    preparedStatement.setInt(salaryColumnNumber, currency.getSalary());
                    preparedStatement.setInt(idColumnNumber, currency.getId());

                    return preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
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
                    preparedStatement.setInt(idColumnNumber, id);

                    return preparedStatement.executeUpdate();
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return 0;
    }
}
