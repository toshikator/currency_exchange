package third_project.DbConnection;


import third_project.entities.Currency;


import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Pattern;


public class CurrenciesDbConnector {

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private final DataSource ds;
    private int idColumnNumber;
    private int codeColumnNumber;
    private int fullNameColumnNumber;
    private int signColumnNumber;
    private String tableName;


    public CurrenciesDbConnector() {

        try {
            Properties props = new Properties();
            try (java.io.InputStream in = CurrenciesDbConnector.class.getClassLoader().getResourceAsStream("configs/db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            }
            String tbl = props.getProperty("currencies.table.name");
            if (tbl == null || tbl.isEmpty()) {
                tbl = props.getProperty("tableNameCurrencies");
            }
            this.tableName = tbl;
            idColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.id"));
            codeColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.code"));
            fullNameColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.fullName"));
            signColumnNumber = Integer.parseInt(props.getProperty("currencies.columns.sign"));


        } catch (Exception ex) {
            System.out.println("Exception in CurrenciesDbConnection constructor");
            System.out.println(ex);
        }
        this.ds = HikariPool.get();
    }

    public Currency insert(String code, String name, String sign) {

        try {

            try (Connection conn = ds.getConnection()) {

                String sql = "INSERT INTO " + tableName + " (CODE, SIGN, FULL_NAME) Values (?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, code.toUpperCase());
                    preparedStatement.setString(2, sign);
                    preparedStatement.setString(3, name);
                    preparedStatement.executeUpdate();
                    return findByCode(code);
                }
            }
        } catch (Exception ex) {
            System.out.println("insert exception: " + ex);
            System.out.println(ex);
        }
        return null;
    }

    public int update(Currency currency) {

        try {

            try (Connection conn = ds.getConnection()) {

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

    public int deleteById(int id) {

        try {

            try (Connection conn = ds.getConnection()) {

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

    public Currency findByCode(String code) throws SQLException {
        Currency currency = null;
        try {

            try (Connection conn = ds.getConnection()) {
                String sql = "SELECT * FROM " + tableName + " WHERE CODE = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setString(1, code.toUpperCase());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            int id = resultSet.getInt(idColumnNumber);
                            String fullName = resultSet.getString(fullNameColumnNumber);
                            String sign = resultSet.getString(signColumnNumber);
                            currency = new Currency(id, code.toUpperCase(), fullName, sign);
                        }
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

    public Currency findById(int id) {
        Currency currency = null;
        try {

            try (Connection conn = ds.getConnection()) {
                String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                    preparedStatement.setInt(1, id);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String code = resultSet.getString(codeColumnNumber);
                            String fullName = resultSet.getString(fullNameColumnNumber);
                            String sign = resultSet.getString(signColumnNumber);
                            currency = new Currency(id, code, fullName, sign);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception in findById");
            System.out.println("Currency id = " + id);
            System.out.println(ex);
        }
        return currency;
    }

    public ArrayList<Currency> getAllCurrencies() {

        ArrayList<Currency> currencies = new ArrayList<Currency>();
        try {

            try (Connection conn = ds.getConnection()) {

                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tableName)) {
                    try (ResultSet resultSet = ps.executeQuery()) {
                        while (resultSet.next()) {
                            int id = resultSet.getInt(idColumnNumber);
                            String code = resultSet.getString(codeColumnNumber);
                            String fullName = resultSet.getString(fullNameColumnNumber);
                            String sign = resultSet.getString(signColumnNumber);
                            Currency currency = new Currency(id, code, fullName, sign);
                            currencies.add(currency);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return currencies;
    }
}
