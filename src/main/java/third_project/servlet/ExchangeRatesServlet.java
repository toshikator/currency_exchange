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
import third_project.service.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CurrenciesDbConnector currenciesDbConnector;
    private ExchangeRatesDbConnector exchangeRatesDbConnector;
    private static final Logger log = Logger.getLogger("com.example");
    public ExchangeRatesServlet() {
        super();
    }

    @Override
    public void init() throws jakarta.servlet.ServletException {
        super.init();
        log.info("ExchangeRatesServlet init");
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
        log.info("ExchangeRatesServlet doGet parameters: " + request.getPathInfo());
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
            if (!Validation.isListValid(exchangeRates)) {
                System.err.println("empty dataset of DTOs");
                throw new IllegalStateException("empty dataset of DTOs");
            }

            mapper.writeValue(response.getWriter(), exchangeRates);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalStateException e) {
            log.info("ExchangeRatesServlet (doGet): empty dataset of DTOs: "+e.getMessage());
            System.err.println("empty dataset of DTOs");
            response.getWriter().println("empty dataset of DTOs");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            log.info("ExchangeRatesServlet (doGet): servlet global error: "+e.getMessage());
            System.err.println("servlet global error: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().println("servlet global error: " + e.getMessage());
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
            log.info("ExchangeRatesServlet doPost parameters: "+Arrays.toString(new String[]{baseCurrencyCode, targetCurrencyCode, rateParam}));

            if (!Validation.areThreeStringsValid(baseCurrencyCode, targetCurrencyCode, rateParam)) {
                throw new IllegalStateException("invalid parameters");
            }

            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();

            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                throw new IllegalStateException("baseCurrencyCode and targetCurrencyCode must be different");
            }

            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);

            if (!Validation.isCurrencyValid(baseCurrency) || !Validation.isCurrencyValid(targetCurrency)) {
                throw new IllegalArgumentException("invalid currency ");
            }

            BigDecimal rate;
            rate = new BigDecimal(rateParam);
            if (Validation.isZeroOrNegative(rate)) {
                throw new NumberFormatException("rate must be positive");
            }

            ExchangeRate existing = ExchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            if (existing != null) {
                System.err.println("exchange rate already exists");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }

            ExchangeRate created = exchangeRatesDbConnector.insert(baseCurrencyCode, targetCurrencyCode, rate);
            if (created == null) {
                throw new Exception("exchange rate not created");
            }

            DTOExchangeRate dto = new DTOExchangeRate(created.getId(), baseCurrency, targetCurrency, rate);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), dto);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (NumberFormatException nfe) {
            System.err.println("rate must be a number");
            response.getWriter().println("rate must be a number" + nfe.getMessage());
            nfe.printStackTrace();
            log.info("ExchangeRatesServlet NumberFormatException exception(doPOST): "+nfe.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.getWriter().println("invalid currency" + e.getMessage());
            System.err.println("invalid currency code");
            log.info("ExchangeRatesServlet IllegalArgumentException exception(doPOST): "+e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            System.err.println("invalid parameters");
            log.info("ExchangeRatesServlet IllegalStateException exception(doPOST): "+e.getMessage());
            response.getWriter().println("invalid parameters" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("error by exchange rate servlet");
            log.info("ExchangeRatesServlet General exception(doPOST): "+e.getMessage());
            response.getWriter().println("error by exchange rate servlet" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
