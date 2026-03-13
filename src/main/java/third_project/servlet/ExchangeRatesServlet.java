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
        //        response.setContentType("application/json");
        //        response.setCharacterEncoding("UTF-8");
        try {
            //            ObjectMapper mapper = new ObjectMapper();
            List<DTOExchangeRate> exchangeRates = exchangeRatesDbConnector.selectAll().parallelStream().map((exchangeRate) -> {
                DTOExchangeRate result = new DTOExchangeRate();
                result.setId(exchangeRate.getId());
                result.setBaseCurrency(currenciesDbConnector.findById(exchangeRate.getBaseCurrencyId()));
                result.setTargetCurrency(currenciesDbConnector.findById(exchangeRate.getTargetCurrencyId()));
                result.setRate(exchangeRate.getRate().setScale(2, RoundingMode.HALF_UP));
                return result;
            }).collect(Collectors.toList());
            if (exchangeRates.isEmpty()) {
                log.info("empty dataset of DTOs [File: ExchangeRatesServlet.java]");
                throw new IllegalStateException("empty dataset of DTOs");
            }
            //
            //            objectMapper.writeValue(response.getWriter(), exchangeRates);
            //            response.setStatus(HttpServletResponse.SC_OK);
            writeJson(response, HttpServletResponse.SC_OK, exchangeRates);
        } catch (IllegalStateException e) {
            log.warning("ExchangeRatesServlet (doGet): empty dataset of DTOs: " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.getWriter().println("empty dataset of DTOs");
            //            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "empty dataset of DTOs" + e.getMessage());

        } catch (Exception e) {
            log.warning("ExchangeRatesServlet (doGet): servlet global error: " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.getWriter().println("servlet global error: " + e.getMessage());
            //            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "servlet global error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //        response.setContentType("application/json");
        //        response.setCharacterEncoding("UTF-8");
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
            String rateParam = request.getParameter("rate");
            //            log.info("ExchangeRatesServlet doPost parameters: " + Arrays.toString(new String[]{baseCurrencyCode, targetCurrencyCode, rateParam}));

            if (!Validation.isStringValid(baseCurrencyCode)
                    && !Validation.isStringValid(targetCurrencyCode)
                    && !Validation.isStringConvertableToBigDecimal(rateParam)) {
                log.warning("ExchangeRatesServlet: invalid parameters: base=" +
                        baseCurrencyCode + ", " +
                        "target=" + targetCurrencyCode + ", rate=" + rateParam + " [File: ExchangeRatesServlet.java]");
                throw new IllegalStateException("invalid parameters");
            }

            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();

            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                log.warning("ExchangeRatesServlet: baseCurrencyCode and targetCurrencyCode must be different: " + baseCurrencyCode + " [File: ExchangeRatesServlet.java]");
                throw new IllegalStateException("baseCurrencyCode and targetCurrencyCode must be different");
            }

            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);

            if (!Validation.isCurrencyValid(baseCurrency) || !Validation.isCurrencyValid(targetCurrency)) {
                log.warning("ExchangeRatesServlet: invalid currency code(s): base=" + baseCurrencyCode + ", target=" + targetCurrencyCode + " [File: ExchangeRatesServlet.java]");
                throw new IllegalArgumentException("invalid currency ");
            }

            BigDecimal rate;
            rate = new BigDecimal(rateParam);
            if (Validation.isZeroOrNegative(rate)) {
                log.warning("ExchangeRatesServlet: rate must be positive: " + rateParam + " [File: ExchangeRatesServlet.java]");
                throw new NumberFormatException("rate must be positive");
            }

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            if (existing != null) {
                log.info("exchange rate already exists [File: ExchangeRatesServlet.java]");
                //                response.setStatus(HttpServletResponse.SC_CONFLICT);
                writeError(response, HttpServletResponse.SC_CONFLICT, "exchange rate already exists");
                return;
            }

            ExchangeRate created = exchangeRatesDbConnector.insert(baseCurrencyCode, targetCurrencyCode, rate);
            if (created == null) {
                throw new Exception("exchange rate not created");
            }

            DTOExchangeRate dto = new DTOExchangeRate(created.getId(), baseCurrency, targetCurrency, rate);

            //            objectMapper.writeValue(response.getWriter(), dto);
            //            response.setStatus(HttpServletResponse.SC_CREATED);
            writeJson(response, HttpServletResponse.SC_CREATED, dto);
        } catch (NumberFormatException nfe) {
            //            log.info("rate must be a number [File: ExchangeRatesServlet.java]");
            //            response.getWriter().println("rate must be a number" + nfe.getMessage());

            log.info("ExchangeRatesServlet NumberFormatException exception(doPOST): " + nfe.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "rate must be a number");
        } catch (IllegalArgumentException e) {
            //            response.getWriter().println("invalid currency" + e.getMessage());
            log.info("ExchangeRatesServlet IllegalArgumentException(invalid currency code) exception(doPOST): " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "invalid currency code");
        } catch (IllegalStateException e) {
            log.info("ExchangeRatesServlet IllegalStateException(invalid parameters) exception(doPOST): " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.getWriter().println("invalid parameters" + e.getMessage());
            //            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameters");
        } catch (Exception e) {
            log.info("ExchangeRatesServlet General exception(doPOST): " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            //            response.getWriter().println("error by exchange rate servlet" + e.getMessage());
            //            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ExchangeRatesServlet General exception(doPOST): " + e.getMessage());
        }
    }
}
