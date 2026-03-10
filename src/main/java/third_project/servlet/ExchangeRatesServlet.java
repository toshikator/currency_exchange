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
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {


    public ExchangeRatesServlet() {
        super();
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
                result.setRate(exchangeRate.getRate().setScale(2, RoundingMode.HALF_UP));
                return result;
            }).collect(Collectors.toList());
            if (exchangeRates.isEmpty()) {
                log.info("empty dataset of DTOs");
                throw new IllegalStateException("empty dataset of DTOs");
            }

            mapper.writeValue(response.getWriter(), exchangeRates);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalStateException e) {
            log.info("ExchangeRatesServlet (doGet): empty dataset of DTOs: " + e.getMessage());
            response.getWriter().println("empty dataset of DTOs");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            log.info("ExchangeRatesServlet (doGet): servlet global error: " + e.getMessage());
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
            log.info("ExchangeRatesServlet doPost parameters: " + Arrays.toString(new String[]{baseCurrencyCode, targetCurrencyCode, rateParam}));

            if (!Validation.areThreeStringsValid(baseCurrencyCode, targetCurrencyCode, rateParam)) {
                log.warning("ExchangeRatesServlet: invalid parameters: base=" + baseCurrencyCode + ", target=" + targetCurrencyCode + ", rate=" + rateParam);
                throw new IllegalStateException("invalid parameters");
            }

            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();

            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                log.warning("ExchangeRatesServlet: baseCurrencyCode and targetCurrencyCode must be different: " + baseCurrencyCode);
                throw new IllegalStateException("baseCurrencyCode and targetCurrencyCode must be different");
            }

            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);

            if (!Validation.isCurrencyValid(baseCurrency) || !Validation.isCurrencyValid(targetCurrency)) {
                log.warning("ExchangeRatesServlet: invalid currency code(s): base=" + baseCurrencyCode + ", target=" + targetCurrencyCode);
                throw new IllegalArgumentException("invalid currency ");
            }

            BigDecimal rate;
            rate = new BigDecimal(rateParam);
            if (Validation.isZeroOrNegative(rate)) {
                log.warning("ExchangeRatesServlet: rate must be positive: " + rateParam);
                throw new NumberFormatException("rate must be positive");
            }

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            if (existing != null) {
                log.info("exchange rate already exists");
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
            log.info("rate must be a number");
            response.getWriter().println("rate must be a number" + nfe.getMessage());
            log.info("ExchangeRatesServlet NumberFormatException exception(doPOST): " + nfe.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            response.getWriter().println("invalid currency" + e.getMessage());
            log.info("ExchangeRatesServlet IllegalArgumentException(invalid currency code) exception(doPOST): " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException e) {
            log.info("ExchangeRatesServlet IllegalStateException(invalid parameters) exception(doPOST): " + e.getMessage());
            response.getWriter().println("invalid parameters" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            log.info("ExchangeRatesServlet General exception(doPOST): " + e.getMessage());
            response.getWriter().println("error by exchange rate servlet" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
