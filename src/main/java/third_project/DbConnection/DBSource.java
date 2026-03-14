package third_project.DbConnection;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import third_project.service.PropertiesReader;


public final class DBSource {
    private static final Logger log = Logger.getLogger("com.example");
    private static volatile DBSource INSTANCE;

    private final String address;
    private final String port;
    private final String databaseName;
    private final String username;
    private final String password;

    private final String currenciesTableName;
    private final int currenciesIdCol;
    private final int currenciesCodeCol;
    private final int currenciesFullNameCol;
    private final int currenciesSignCol;

    private final String exchangeRatesTableName;
    private final int exchangeRatesIdCol;
    private final int baseCurrencyIdCol;
    private final int targetCurrencyIdCol;
    private final int rateCol;

    private final DataSource ds;

    private DBSource(PropertiesReader pr) {
        this.address = pr.getAddress();
        this.port = pr.getPort();
        this.databaseName = pr.getDatabaseName();
        this.username = pr.getUsername();
        this.password = pr.getPassword();

        this.currenciesTableName = pr.getCurrenciesTableName();
        this.currenciesIdCol = pr.getCurrenciesIdCol();
        this.currenciesCodeCol = pr.getCurrenciesCodeCol();
        this.currenciesFullNameCol = pr.getCurrenciesFullNameCol();
        this.currenciesSignCol = pr.getCurrenciesSignCol();

        this.exchangeRatesTableName = pr.getExchangeRatesTableName();
        this.exchangeRatesIdCol = pr.getExchangeRatesIdCol();
        this.baseCurrencyIdCol = pr.getBaseCurrencyIdCol();
        this.targetCurrencyIdCol = pr.getTargetCurrencyIdCol();
        this.rateCol = pr.getRateCol();

        HikariPool.init(pr);
        this.ds = HikariPool.get();
    }

    public static void init(PropertiesReader pr) {
        if (pr == null) {
            log.log(Level.SEVERE, "PropertiesReader must not be null");
            throw new IllegalArgumentException("PropertiesReader must not be null");
        }
        DBSource local = INSTANCE;
        if (local == null) {
            synchronized (DBSource.class) {
                local = INSTANCE;
                if (local == null) {
                    local = new DBSource(pr);
                    INSTANCE = local;
                }
            }
        }
    }

    public static DBSource getInstance() {
        DBSource local = INSTANCE;
        if (local == null) {
            Logger.getLogger(DBSource.class.getName()).log(Level.SEVERE, "DBSource not initialized. Call DBSource.init(PropertiesReader) first (e.g., from ContextListener).");
            throw new IllegalStateException("DBSource not initialized. Call DBSource.init(PropertiesReader) first (e.g., from ContextListener).");
        }
        return local;
    }

    public static DataSource get() {
        return getInstance().getDataSource();
    }

    public static void shutdown() {
        try {
            HikariPool.shutdown();
        } finally {
            INSTANCE = null;
        }
    }

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCurrenciesTableName() {
        return currenciesTableName;
    }

    public int getCurrenciesIdCol() {
        return currenciesIdCol;
    }

    public int getCurrenciesCodeCol() {
        return currenciesCodeCol;
    }

    public int getCurrenciesFullNameCol() {
        return currenciesFullNameCol;
    }

    public int getCurrenciesSignCol() {
        return currenciesSignCol;
    }

    public String getExchangeRatesTableName() {
        return exchangeRatesTableName;
    }

    public int getExchangeRatesIdCol() {
        return exchangeRatesIdCol;
    }

    public int getBaseCurrencyIdCol() {
        return baseCurrencyIdCol;
    }

    public int getTargetCurrencyIdCol() {
        return targetCurrencyIdCol;
    }

    public int getRateCol() {
        return rateCol;
    }

    public DataSource getDataSource() {
        return ds;
    }
}
