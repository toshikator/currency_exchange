package third_project.DbConnection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import third_project.service.PropertiesReader;

public final class HikariPool {
    private static final Logger log = Logger.getLogger("com.example");
    private static volatile HikariPool INSTANCE;
    private static volatile PropertiesReader PR_REF;

    private final HikariDataSource ds;
    private final String username;
    private final String password;
    private final String url;

    private HikariPool(PropertiesReader pr) {
        try {
            log.info("Initializing HikariCP pool...");
            String address = pr.getAddress();
            String port = pr.getPort();
            String databaseName = pr.getDatabaseName();
            username = pr.getUsername();
            password = pr.getPassword();

            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException cnfe) {
                log.log(Level.SEVERE, "Oracle JDBC driver class not found. Ensure ojdbc JAR is on the classpath.", cnfe);
                throw new RuntimeException("Oracle JDBC driver class not found. Ensure ojdbc JAR is on the classpath.", cnfe);
            }

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);

            cfg.setDriverClassName("oracle.jdbc.OracleDriver");
            cfg.setPoolName("app-pool");

            cfg.setInitializationFailTimeout(-1);
            cfg.setMinimumIdle(0);
            ds = new HikariDataSource(cfg);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.info("Shutting down HikariCP pool...");
                    HikariPool.shutdown();
                } catch (Throwable t) {
                    log.log(Level.SEVERE, "Error while shutting down HikariCP", t);
                }
            }, "hikari-shutdown"));

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to initialize HikariCP pool", e);
            throw new RuntimeException(e);
        }
    }

    public static void init(PropertiesReader pr) {
        if (pr == null) {
            log.log(Level.SEVERE, "PropertiesReader must not be null");
            throw new IllegalArgumentException("PropertiesReader must not be null");
        }
        synchronized (HikariPool.class) {
            if (PR_REF == null) {
                PR_REF = pr;
            }
            if (INSTANCE == null) {
                INSTANCE = new HikariPool(PR_REF);
            }
        }
    }

    public static DataSource get() {
        HikariPool local = INSTANCE;
        if (local == null) {
            synchronized (HikariPool.class) {
                local = INSTANCE;
                if (local == null) {
                    if (PR_REF == null) {
                        log.log(Level.SEVERE, "HikariPool not initialized. Call HikariPool.init(PropertiesReader) from ContextListener before use.");
                        throw new IllegalStateException("HikariPool not initialized. Call HikariPool.init(PropertiesReader) from ContextListener before use.");
                    }
                    local = new HikariPool(PR_REF);
                    INSTANCE = local;
                }
            }
        }
        // Do not log sensitive data like credentials.
        log.info("HikariPool is ready (DataSource initialized).");
        return local.ds;
    }

    public static void shutdown() {
        HikariPool local = INSTANCE;
        if (local != null) {
            try {
                local.ds.close();
            } finally {
                INSTANCE = null;
            }
        }
    }
}
