package third_project.contexts;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.HikariPool;

import javax.sql.DataSource;
import java.util.Properties;


@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CurrenciesDbConnector currenciesDbConnector = new CurrenciesDbConnector();
        sce.getServletContext().setAttribute("currenciesDbConnector", currenciesDbConnector);
    }


}