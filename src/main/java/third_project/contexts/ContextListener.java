package third_project.contexts;


import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.DbConnection.DBSource;
import third_project.service.PropertiesReader;

import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class ContextListener implements ServletContextListener {

    private static final Logger log = Logger.getLogger("com.example");

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            // Initialize PropertiesReader once and expose via context
            PropertiesReader propertiesReader = PropertiesReader.getInstance();
            ctx.setAttribute("propertiesReader", propertiesReader);

            // Initialize and expose a shared DBSource (wraps pooled DataSource)
            DBSource.init(propertiesReader);

            // Initialize and expose DAO/connector singletons
            CurrenciesDbConnector currenciesDbConnector = new CurrenciesDbConnector(propertiesReader);
            ctx.setAttribute("currenciesDbConnector", currenciesDbConnector);

            ExchangeRatesDbConnector exchangeRatesDbConnector = new ExchangeRatesDbConnector(propertiesReader, currenciesDbConnector);
            ctx.setAttribute("exchangeRatesDbConnector", exchangeRatesDbConnector);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Failed to initialize application context", t);
            // Let the container see the failure with full cause
            throw new RuntimeException("Failed to initialize application context", t);
        }
    }
}