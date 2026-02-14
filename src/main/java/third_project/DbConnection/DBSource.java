package third_project.DbConnection;

import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import third_project.service.PropertiesReader;

/**
 * Singleton facade for application DataSource.
 *
 * This class delegates pool creation and lifecycle to {@link HikariPool}
 * and exposes a simple singleton access pattern.
 */
public final class DBSource {
    private static final Logger log = Logger.getLogger("com.example");
    private static volatile DBSource INSTANCE;
    private final DataSource ds;

    private DBSource(PropertiesReader pr) {
        // Delegate initialization to HikariPool to avoid duplicating config logic
        HikariPool.init(pr);
        this.ds = HikariPool.get();
    }

    /**
     * Initialize the DBSource singleton with application properties.
     * Safe to call multiple times; only the first call performs initialization.
     */
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

    /**
     * Returns the singleton instance. Make sure {@link #init(PropertiesReader)} was called earlier.
     */
    public static DBSource getInstance() {
        DBSource local = INSTANCE;
        if (local == null) {
            Logger.getLogger(DBSource.class.getName()).log(Level.SEVERE, "DBSource not initialized. Call DBSource.init(PropertiesReader) first (e.g., from ContextListener).");
            throw new IllegalStateException("DBSource not initialized. Call DBSource.init(PropertiesReader) first (e.g., from ContextListener).");
        }
        return local;
    }

    /**
     * Convenience accessor for the underlying DataSource.
     */
    public DataSource getDataSource() {
        return ds;
    }

    /**
     * Convenience static accessor that returns the shared DataSource directly.
     */
    public static DataSource get() {
        return getInstance().getDataSource();
    }

    /**
     * Shutdown underlying pool and reset singleton.
     */
    public static void shutdown() {
        try {
            HikariPool.shutdown();
        } finally {
            INSTANCE = null;
        }
    }
}
