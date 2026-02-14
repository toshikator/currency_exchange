package third_project.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class PropertiesReader {
    private static final Logger log = Logger.getLogger("third_project.PropertiesReader");
    private static volatile PropertiesReader INSTANCE;


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

    private PropertiesReader() {
        try {
            Properties connectivity = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("configs/connectivity.properties")) {
                if (in != null) {
                    connectivity.load(in);
                } else {
                    log.severe("configs/connectivity.properties not found on classpath");
                    throw new IllegalStateException("configs/connectivity.properties not found on classpath");
                }
            }

            Properties db = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("configs/db.properties")) {
                if (in != null) {
                    db.load(in);
                } else {
                    log.severe("configs/db.properties not found on classpath");
                    throw new IllegalStateException("configs/db.properties not found on classpath");
                }
            }


            this.address = required(connectivity, "address");
            this.port = required(connectivity, "port");
            this.databaseName = required(connectivity, "databaseName");
            this.username = required(connectivity, "username");
            this.password = required(connectivity, "password");


            String tblCurrencies = db.getProperty("currencies.table.name");
            if (tblCurrencies == null || tblCurrencies.isEmpty()) {
                tblCurrencies = required(db, "tableNameCurrencies");
            }
            this.currenciesTableName = tblCurrencies;
            this.currenciesIdCol = parseInt(required(db, "currencies.columns.id"), "currencies.columns.id");
            this.currenciesCodeCol = parseInt(required(db, "currencies.columns.code"), "currencies.columns.code");
            this.currenciesFullNameCol = parseInt(required(db, "currencies.columns.fullName"), "currencies.columns.fullName");
            this.currenciesSignCol = parseInt(required(db, "currencies.columns.sign"), "currencies.columns.sign");


            this.exchangeRatesTableName = required(db, "tableNameExchangeRates");
            this.exchangeRatesIdCol = parseInt(required(db, "exchangeRates.columns.id"), "exchangeRates.columns.id");
            this.baseCurrencyIdCol = parseInt(required(db, "exchangeRates.columns.baseCurrencyId"), "exchangeRates.columns.baseCurrencyId");
            this.targetCurrencyIdCol = parseInt(required(db, "exchangeRates.columns.targetCurrencyId"), "exchangeRates.columns.targetCurrencyId");
            this.rateCol = parseInt(required(db, "exchangeRates.columns.rate"), "exchangeRates.columns.rate");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to load application properties", e);
            throw new RuntimeException("Failed to load application properties", e);
        }
    }

    private static String required(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            log.severe("Required property missing: " + key);
            throw new IllegalStateException("Required property missing: " + key);
        }
        return v.trim();
    }

    private static int parseInt(String v, String key) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            log.log(Level.SEVERE, "Invalid integer for property '{0}': '{1}'", new Object[]{key, v});
            throw new IllegalStateException("Invalid integer for property '" + key + "': '" + v + "'", nfe);
        }
    }

    public static PropertiesReader getInstance() {
        PropertiesReader local = INSTANCE;
        if (local == null) {
            synchronized (PropertiesReader.class) {
                local = INSTANCE;
                if (local == null) {
                    local = new PropertiesReader();
                    INSTANCE = local;
                }
            }
        }
        return local;
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
}
