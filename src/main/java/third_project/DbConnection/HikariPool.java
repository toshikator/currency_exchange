package third_project.DbConnection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.regex.Pattern;

public class HikariPool {
    private static final HikariDataSource DS;
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final String username;
    private static final String password;
    private static final String tableName;
    private static final String url;

    static {
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
            if (tableName == null || !SAFE_IDENTIFIER.matcher(tableName).matches()) {
                throw new IllegalStateException("Unsafe table name for: " + tableName);
            }

            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            cfg.setPoolName("app-pool");
            DS = new HikariDataSource(cfg);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down HikariCP pool...");
                    HikariPool.shutdown();
                } catch (Throwable t) {
                    System.err.println("Error while shutting down HikariCP: " + t);
                }
            }, "hikari-shutdown"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HikariPool() {
    }

    public static DataSource get() {
        return DS;
    }

    public static void shutdown() {
        DS.close();
    }
}
