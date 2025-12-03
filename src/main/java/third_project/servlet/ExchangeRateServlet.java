package third_project.servlet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchangeRate;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.Validation;


import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "exchangerate", value = "/exchangerate/*")
public class ExchangeRateServlet extends HttpServlet {
    private CurrenciesDbConnector currenciesDbConnector;
    private ExchangeRatesDbConnector exchangeRatesDbConnector;

    public ExchangeRateServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null)
            throw new ServletException("currenciesDbConnector not found in ServletContext");
        exchangeRatesDbConnector = (ExchangeRatesDbConnector) getServletContext().getAttribute("exchangeRatesDbConnector");
        if (exchangeRatesDbConnector == null)
            throw new ServletException("exchangeRatesDbConnector not found in ServletContext");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 7) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String path = pathInfo.substring(1);
            String baseCurrencyCode = path.substring(0, 3).toUpperCase();
            String targetCurrencyCode = path.substring(3, 6).toUpperCase();
            if (baseCurrencyCode == null || targetCurrencyCode == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            PrintWriter out = response.getWriter();
            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);
            ExchangeRate rate = ExchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            out.println(new DTOExchangeRate(rate.getId(), baseCurrency, targetCurrency, rate.getRate()));
            response.getWriter().println(new ObjectMapper().writeValueAsString(new DTOExchangeRate(rate.getId(), baseCurrency, targetCurrency, rate.getRate())));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("loloL " + e);
            throw new ServletException("Error processing exchange rate request", e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String pathInfo = request.getPathInfo().toUpperCase();
            System.out.println("pathInfo: " + pathInfo);
            if (!Validation.isPatchRequestValid(pathInfo)) {
                throw new IllegalArgumentException("Invalid pathInfo");
            }

            String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            String targetCurrencyCode = pathInfo.substring(4).toUpperCase();
            System.out.println("baseCurrencyCode: " + baseCurrencyCode + " targetCurrencyCode: " + targetCurrencyCode);


            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> rates = new HashMap<>();
            mapper.readValues((HashMap<String, String>) rates, String.class);
            rates.forEach((key, value) -> System.out.println(key + " -> " + value));

            String rateParam = request.getParameter("rate");
            System.out.println("rateParam: " + rateParam);
            if (Validation.isStringValid(rateParam)) {
                System.err.println("String is invalid");
                throw new IllegalArgumentException("Invalid rate parameter");
            }

            Validation.isStringConvertableToBigDecimal(rateParam);
            System.out.println("rateParam: " + rateParam);
            BigDecimal newRate = new BigDecimal(rateParam);

            if (Validation.isZeroOrNegative(newRate)) {
                throw new IllegalArgumentException("Invalid rate value");
            }

            int baseCurrencyId = 0;
            int targetCurrencyId = 0;
            baseCurrencyId = currenciesDbConnector.findByCode(baseCurrencyCode).getId();
            targetCurrencyId = currenciesDbConnector.findByCode(targetCurrencyCode).getId();

            ExchangeRate existing = ExchangeRatesDbConnector.findRate(baseCurrencyId, targetCurrencyId);
            if (existing == null) {
                throw new NullPointerException("Exchange rate not found");
            }

            ExchangeRate updated = exchangeRatesDbConnector.update(baseCurrencyId, targetCurrencyId, newRate);
            if (updated == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            System.out.println("CurrencyExchange rate updated:" + updated);
            PrintWriter out = response.getWriter();
            out.println(new DTOExchangeRate(updated.getId(), currenciesDbConnector.findById(baseCurrencyId), currenciesDbConnector.findById(targetCurrencyId), updated.getRate()));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
            response.getWriter().println(e.getMessage() + " Null pointer exception");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error updating exchange rate", e);
        }
    }
}
