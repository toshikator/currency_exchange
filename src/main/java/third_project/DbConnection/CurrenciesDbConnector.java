package third_project.DbConnection;


import third_project.entities.Currency;
import third_project.service.PropertiesReader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class CurrenciesDbConnector {

    private static final Logger log = Logger.getLogger("com.example");
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private final PropertiesReader pr;


    public CurrenciesDbConnector(PropertiesReader pr) {
        this.pr = pr;

    }

    public Optional<Currency> insert(String code, String name, String sign) {
        String sql = "INSERT INTO " + pr.getCurrenciesTableName() + " (CODE, SIGN, FULL_NAME) VALUES (?, ?, ?)";
        try (Connection conn = DBSource.get().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.toUpperCase());
            ps.setString(2, sign);
            ps.setString(3, name);
            ps.executeUpdate();


            return findByCode(code);
        } catch (SQLException ex) {
            log.warning("insert exception: " + ex + "File: CurrenciesDbConnector.java");
        }
        return Optional.empty();
    }

    public int update(Currency currency) throws SQLException {
        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "UPDATE " + pr.getCurrenciesTableName() + " SET CODE = ?, SIGN = ?, FULL_NAME = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, currency.getCode().toUpperCase());
                preparedStatement.setString(2, currency.getSign());
                preparedStatement.setString(3, currency.getName());
                preparedStatement.setInt(4, currency.getId());
                return preparedStatement.executeUpdate();
            }
        }
    }

    public int deleteById(int id) {
        try (Connection conn = DBSource.get().getConnection()) {
            String sql = "DELETE FROM " + pr.getCurrenciesTableName() + " WHERE id = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);
                return preparedStatement.executeUpdate();
            }
        } catch (Exception ex) {
            log.warning("delete exception: " + ex + " [File: CurrenciesDbConnector.java]");

        }
        return 0;
    }

    public Optional<Currency> findByCode(String code) throws SQLException {
        try (Connection conn = DBSource.get().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + pr.getCurrenciesTableName() + " WHERE CODE = ?")) {
            ps.setString(1, code.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(pr.getCurrenciesIdCol());
                    String fullName = rs.getString(pr.getCurrenciesFullNameCol());
                    String sign = rs.getString(pr.getCurrenciesSignCol());
                    return Optional.ofNullable(new Currency(id, code.toUpperCase(), fullName, sign));
                }
            }

        }
        return Optional.empty();
    }

    public Optional<Currency> findById(int id) throws SQLException {
        try (Connection conn = DBSource.get().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + pr.getCurrenciesTableName() + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String code = rs.getString(pr.getCurrenciesCodeCol());
                    String fullName = rs.getString(pr.getCurrenciesFullNameCol());
                    String sign = rs.getString(pr.getCurrenciesSignCol());
                    return Optional.ofNullable(new Currency(id, code, fullName, sign));
                }
            }
        }
        return Optional.empty();
    }

    public ArrayList<Currency> getAllCurrencies() throws SQLException {
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
        }
        return currencies;
    }
}
