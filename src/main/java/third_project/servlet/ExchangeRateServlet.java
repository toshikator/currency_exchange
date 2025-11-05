package third_project.servlet;

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


import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;


@WebServlet(name = "exchangerate", value = "/exchangerate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final CurrenciesDbConnector currenciesDbConnector;
    private final ExchangeRatesDbConnector exchangeRatesDbConnector;

    public ExchangeRateServlet() {
        super();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        exchangeRatesDbConnector = (ExchangeRatesDbConnector) getServletContext().getAttribute("exchangeRatesDbConnector");
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
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 7) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String path = pathInfo.substring(1);
            String baseCurrencyCode = path.substring(0, 3).toUpperCase();
            String targetCurrencyCode = path.substring(3, 6).toUpperCase();
            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String rateParam = request.getParameter("rate");
            if (rateParam == null || rateParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            BigDecimal newRate;
            try {
                newRate = new BigDecimal(rateParam);
            } catch (NumberFormatException nfe) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (newRate.compareTo(BigDecimal.ZERO) <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            int baseCurrencyId = 0;
            int targetCurrencyId = 0;
            try {
                baseCurrencyId = currenciesDbConnector.findByCode(baseCurrencyCode).getId();
                targetCurrencyId = currenciesDbConnector.findByCode(targetCurrencyCode).getId();
            } catch (NullPointerException npe) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ExchangeRate existing = ExchangeRatesDbConnector.findRate(baseCurrencyId, targetCurrencyId);
            if (existing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ExchangeRate updated = exchangeRatesDbConnector.update(baseCurrencyId, targetCurrencyId, newRate);
            if (updated == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            PrintWriter out = response.getWriter();
            out.println(new DTOExchangeRate(updated.getId(), currenciesDbConnector.findById(baseCurrencyId), currenciesDbConnector.findById(targetCurrencyId), updated.getRate()));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error updating exchange rate", e);
        }
    }
}
