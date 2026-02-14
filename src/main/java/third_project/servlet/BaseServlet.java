package third_project.servlet;

import jakarta.servlet.ServletException;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;

import java.util.logging.Logger;

public class BaseServlet extends jakarta.servlet.http.HttpServlet {
    protected static final Logger log = Logger.getLogger("com.example");
    protected CurrenciesDbConnector currenciesDbConnector;
    protected ExchangeRatesDbConnector exchangeRatesDbConnector;

    @Override
    public void init() throws ServletException {
        super.init();
        log.info("ExchangeRateServlet init");
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null) {
            log.severe("currenciesDbConnector not found in ServletContext");
            throw new ServletException("currenciesDbConnector not found in ServletContext");
        }
        exchangeRatesDbConnector = (ExchangeRatesDbConnector) getServletContext().getAttribute("exchangeRatesDbConnector");
        if (exchangeRatesDbConnector == null) {
            log.severe("exchangeRatesDbConnector not found in ServletContext");
            throw new ServletException("exchangeRatesDbConnector not found in ServletContext");
        }
    }
}
