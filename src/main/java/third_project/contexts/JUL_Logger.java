package third_project.contexts;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

@WebListener
public class JUL_Logger implements ServletContextListener {

    private FileHandler fileHandler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Put logs under: <tomcat>/logs/<your-app-name>/
            String catalinaBase = System.getProperty("catalina.base");
            String appName = sce.getServletContext().getContextPath();
            if (appName == null || appName.isBlank() || "/".equals(appName)) appName = "ROOT";
            else appName = appName.replace("/", "");

            File logDir = new File(catalinaBase, "logs" + File.separator + appName);
            if (!logDir.exists() && !logDir.mkdirs()) {
                throw new IOException("Cannot create log directory: " + logDir.getAbsolutePath());
            }

            // Create/append to file
            File logFile = new File(logDir, "app.log");

            // Configure a dedicated app logger (avoid messing with global root logger too much)
            Logger appLogger = Logger.getLogger("com.example"); // use your base package
            appLogger.setUseParentHandlers(false); // prevent double logging to console

            // Avoid adding multiple handlers on redeploy
            for (Handler h : appLogger.getHandlers()) {
                appLogger.removeHandler(h);
                try { h.close(); } catch (Exception ignored) {}
            }

            fileHandler = new FileHandler(logFile.getAbsolutePath(), true);
            fileHandler.setEncoding("UTF-8");
            fileHandler.setFormatter(new SimpleFormatter());

            appLogger.addHandler(fileHandler);
            appLogger.setLevel(Level.INFO);

            appLogger.info("JUL logging initialized. Log file: " + logFile.getAbsolutePath());

        } catch (Exception e) {
            // If logging isn't ready, at least print to stderr
            System.err.println("Failed to init JUL logging: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            Logger appLogger = Logger.getLogger("com.example");
            if (fileHandler != null) {
                appLogger.removeHandler(fileHandler);
                fileHandler.close();
            }
        } catch (Exception ignored) {}
    }
}
