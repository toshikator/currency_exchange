package third_project.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.entities.Currency;
import third_project.dto.DTOExchangeRate;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CurrenciesDbConnector currenciesDbConnector;
    private ExchangeRatesDbConnector exchangeRatesDbConnector;

    public ExchangeRatesServlet() {
        super();
    }

    @Override
    public void init() throws jakarta.servlet.ServletException {
        super.init();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null) {
            throw new jakarta.servlet.ServletException("currenciesDbConnector not found in ServletContext");
        }
        exchangeRatesDbConnector = (ExchangeRatesDbConnector) getServletContext().getAttribute("exchangeRatesDbConnector");
        if (exchangeRatesDbConnector == null) {
            throw new jakarta.servlet.ServletException("exchangeRatesDbConnector not found in ServletContext");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<DTOExchangeRate> exchangeRates = exchangeRatesDbConnector.selectAll().parallelStream().map((exchangeRate) -> {
                DTOExchangeRate result = new DTOExchangeRate();
                result.setId(exchangeRate.getId());
                result.setBaseCurrency(currenciesDbConnector.findById(exchangeRate.getBaseCurrencyId()));
                result.setTargetCurrency(currenciesDbConnector.findById(exchangeRate.getTargetCurrencyId()));
                result.setRate(exchangeRate.getRate());
                return result;
            }).collect(Collectors.toList());
            if (exchangeRates == null || exchangeRates.isEmpty()) {
                System.err.println("empty dataset of DTOs");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            mapper.writeValue(response.getWriter(), exchangeRates);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.err.println("servlet global error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
            String rateParam = request.getParameter("rate");

            if (baseCurrencyCode == null || targetCurrencyCode == null || rateParam == null
                    || baseCurrencyCode.trim().isEmpty() || targetCurrencyCode.trim().isEmpty() || rateParam.trim().isEmpty()) {
                System.err.println("invalid parameters");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();

            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                System.err.println("base and target currencies must be different");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);

            if (baseCurrency == null || targetCurrency == null) {
                System.err.println("invalid currency code");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(rateParam);
            } catch (NumberFormatException nfe) {
                System.err.println("rate must be a number");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println("rate must be positive");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            ExchangeRate existing = ExchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            if (existing != null) {
                System.err.println("exchange rate already exists");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }

            ExchangeRate created = exchangeRatesDbConnector.insert(baseCurrencyCode, targetCurrencyCode, rate);
            if (created == null) {
                System.err.println("error while inserting exchange rate");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            DTOExchangeRate dto = new DTOExchangeRate(created.getId(), baseCurrency, targetCurrency, rate);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), dto);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
