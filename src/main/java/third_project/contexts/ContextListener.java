package third_project.contexts;


import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.DbConnection.HikariPool;

import javax.sql.DataSource;

@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        try {
            // Initialize and expose a shared DataSource
            DataSource ds = HikariPool.get();
            ctx.setAttribute("datasource", ds);

            // Initialize and expose DAO/connector singletons
            CurrenciesDbConnector currenciesDbConnector = new CurrenciesDbConnector();
            ctx.setAttribute("currenciesDbConnector", currenciesDbConnector);

            ExchangeRatesDbConnector exchangeRatesDbConnector = new ExchangeRatesDbConnector();
            ctx.setAttribute("exchangeRatesDbConnector", exchangeRatesDbConnector);
        } catch (Throwable t) {
            // Let the container see the failure with full cause
            throw new RuntimeException("Failed to initialize application context", t);
        }
    }
}