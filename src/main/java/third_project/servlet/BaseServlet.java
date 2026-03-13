package third_project.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class BaseServlet extends jakarta.servlet.http.HttpServlet {
    protected static final Logger log = Logger.getLogger("com.example");
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected CurrenciesDbConnector currenciesDbConnector;
    protected ExchangeRatesDbConnector exchangeRatesDbConnector;

    protected void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), body);
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        writeJson(response, status, Map.of("message", message));
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.info("ExchangeRateServlet init [File: BaseServlet.java]");
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
