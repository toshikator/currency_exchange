package third_project.DbConnection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public final class HikariPool {
    private static volatile HikariPool INSTANCE;

    private final HikariDataSource ds;
    private final String username;
    private final String password;
    private final String url;

    private HikariPool() {
        try {
            System.out.println("Initializing HikariCP pool...");
            Properties props = new Properties();
            try (java.io.InputStream in = HikariPool.class.getClassLoader().getResourceAsStream("configs/connectivity.properties")) {
                if (in != null) {
                    props.load(in);
                    System.out.println("HikariCP pool initialized successfully");
                }
            } catch (IOException e) {
                System.out.println("Failed to load database configuration for HikariCP");
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
            String address = props.getProperty("address");
            String port = props.getProperty("port");
            String databaseName = props.getProperty("databaseName");
            username = props.getProperty("username");
            password = props.getProperty("password");

            url = String.format("jdbc:oracle:thin:@//%s:%s/%s", address, port, databaseName);
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("Oracle JDBC driver class not found. Ensure ojdbc JAR is on the classpath.", cnfe);
            }

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(username);
            cfg.setPassword(password);
            // Explicitly declare the driver so Hikari doesn't rely on auto-detection
            cfg.setDriverClassName("oracle.jdbc.OracleDriver");
            cfg.setPoolName("app-pool");
            // Do not fail deployment if DB is temporarily unavailable
            cfg.setInitializationFailTimeout(-1);
            cfg.setMinimumIdle(0);
            ds = new HikariDataSource(cfg);

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

    public static DataSource get() {
        HikariPool local = INSTANCE;
        if (local == null) {
            synchronized (HikariPool.class) {
                local = INSTANCE;
                if (local == null) {
                    local = new HikariPool();
                    INSTANCE = local;
                }
            }
        }
        System.out.println("Hikari pool data");
        System.out.println("Address: " + local.url);
        System.out.println("Username: " + local.username);
        System.out.println("Password: " + local.password);
        System.out.println(local.ds.toString());
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
