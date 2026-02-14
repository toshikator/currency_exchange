package third_project.DbConnection;


import third_project.entities.Currency;
import third_project.service.PropertiesReader;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.logging.Logger;


public class CurrenciesDbConnector {

    private static final Logger log = Logger.getLogger("com.example");

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private final DataSource ds;
    private final int idColumnNumber;
    private final int codeColumnNumber;
    private final int fullNameColumnNumber;
    private final int signColumnNumber;
    private final String tableName;


    public CurrenciesDbConnector() {
        PropertiesReader pr = PropertiesReader.getInstance();
        this.tableName = pr.getCurrenciesTableName();
        this.idColumnNumber = pr.getCurrenciesIdCol();
        this.codeColumnNumber = pr.getCurrenciesCodeCol();
        this.fullNameColumnNumber = pr.getCurrenciesFullNameCol();
        this.signColumnNumber = pr.getCurrenciesSignCol();
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
            log.info("insert exception: " + ex);
            log.info(String.valueOf(ex));
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
            log.info("update exception: " + ex);
            log.info(String.valueOf(ex));
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
            log.info("delete exception: " + ex);
            log.info(String.valueOf(ex));
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
            log.info("Exception in selectOne by Currency Code");
            log.info("Currency code = " + code);
            log.info(String.valueOf(ex));
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
            log.info("Exception in findById");
            log.info("Currency id = " + id);
            log.info(String.valueOf(ex));
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
            log.info(String.valueOf(ex));
        }
        return currencies;
    }
}
