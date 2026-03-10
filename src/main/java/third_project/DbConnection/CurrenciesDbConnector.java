package third_project.DbConnection;


import third_project.entities.Currency;
import third_project.service.PropertiesReader;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CurrenciesDbConnector {

    private static final Logger log = Logger.getLogger("com.example");
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private final PropertiesReader pr;


    public CurrenciesDbConnector(PropertiesReader pr) {
        this.pr = pr;

    }

    public Currency insert(String code, String name, String sign) {


        try (Connection conn = DBSource.get().getConnection()) {

            String sql = "INSERT INTO " + pr.getCurrenciesTableName() + " (CODE, SIGN, FULL_NAME) Values (?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, code.toUpperCase());
                preparedStatement.setString(2, sign);
                preparedStatement.setString(3, name);
                preparedStatement.executeUpdate();
                return findByCode(code);

            }
        } catch (Exception ex) {
            log.info("insert exception: " + ex);
        }
        return null;
    }

    public int update(Currency currency) {
        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "UPDATE " + pr.getCurrenciesTableName() + " SET CODE = ?, SIGN = ?, FULL_NAME = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, currency.getCode().toUpperCase());
                preparedStatement.setString(2, currency.getSign());
                preparedStatement.setString(3, currency.getFullName());
                preparedStatement.setInt(4, currency.getId());
                return preparedStatement.executeUpdate();
            }
        } catch (Exception ex) {
            log.info("update exception: " + ex);
        }
        return 0;
    }

    public int deleteById(int id) {
        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "DELETE FROM " + pr.getCurrenciesTableName() + " WHERE id = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);
                return preparedStatement.executeUpdate();
            }
        } catch (Exception ex) {
            log.info("delete exception: " + ex);
            log.info(String.valueOf(ex));
        }
        return 0;
    }

    public Currency findByCode(String code) throws SQLException {
        Currency currency = null;


        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "SELECT * FROM " + pr.getCurrenciesTableName() + " WHERE CODE = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, code.toUpperCase());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt(pr.getCurrenciesIdCol());
                        String fullName = resultSet.getString(pr.getCurrenciesFullNameCol());
                        String sign = resultSet.getString(pr.getCurrenciesSignCol());
                        currency = new Currency(id, code.toUpperCase(), fullName, sign);
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
        try (Connection conn = DBSource.get().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + pr.getCurrenciesTableName() + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString(pr.getCurrenciesCodeCol());
                    String fullName = rs.getString(pr.getCurrenciesFullNameCol());
                    String sign = rs.getString(pr.getCurrenciesSignCol());
                    return new Currency(id, code, fullName, sign);
                }
            }
        } catch (Exception ex) {
            log.info("Exception in findById");
            log.info("Currency id = " + id);
            log.info(String.valueOf(ex));
        }
        return null;
    }

    public ArrayList<Currency> getAllCurrencies() {
        ArrayList<Currency> currencies = new ArrayList<Currency>();
        try (Connection conn = DBSource.get().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + pr.getCurrenciesTableName());
             ResultSet resultSet = ps.executeQuery()
        ) {
            while (resultSet.next()) {
                int id = resultSet.getInt(pr.getCurrenciesIdCol());
                String code = resultSet.getString(pr.getCurrenciesCodeCol());
                String fullName = resultSet.getString(pr.getCurrenciesFullNameCol());
                String sign = resultSet.getString(pr.getCurrenciesSignCol());
                Currency currency = new Currency(id, code, fullName, sign);
                currencies.add(currency);
            }
        } catch (Exception ex) {
            log.info(String.valueOf(ex));
        }
        return currencies;
    }
}
