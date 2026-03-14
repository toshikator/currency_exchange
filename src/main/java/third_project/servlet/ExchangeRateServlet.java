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
import third_project.service.PatchBodyParser;
import third_project.service.Validation;


import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


@WebServlet(name = "exchangeRate", value = "/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {


    public ExchangeRateServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 7) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }
            String path = pathInfo.substring(1);
            String baseCurrencyCode = path.substring(0, 3).toUpperCase();
            String targetCurrencyCode = path.substring(3, 6).toUpperCase();
            if (baseCurrencyCode == null || targetCurrencyCode == null) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }
            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);
            ExchangeRate rate = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            DTOExchangeRate dto = new DTOExchangeRate(rate.getId(), baseCurrency, targetCurrency, rate.getRate().setScale(2, RoundingMode.HALF_UP));

            writeJson(response, HttpServletResponse.SC_OK, dto);
        } catch (NullPointerException e) {
            log.info("ExchangeRate servlet NullPointerException(doGET_1): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
        } catch (Exception e) {
            log.info("ExchangeRate servlet unexpected Exception(doGET_2): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            throw new ServletException("Error processing exchange rate request", e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo().toUpperCase();
            log.info("pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");

            if (!Validation.isPatchRequestValid(pathInfo)) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                throw new IllegalArgumentException("Invalid pathInfo");
            }

            String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            String targetCurrencyCode = pathInfo.substring(4).toUpperCase();
            log.info("baseCurrencyCode: " + baseCurrencyCode + " targetCurrencyCode: " + targetCurrencyCode + " [File: ExchangeRateServlet.java]");

            PatchBodyParser.ParsedBody parsedBody = PatchBodyParser.parse(request);

            String rateParam = request.getParameter("rate");
            if (rateParam == null || rateParam.isBlank()) {
                rateParam = parsedBody.getFirst("rate");
            }
            if (rateParam == null || rateParam.isBlank()) {
                Object jsonRate = parsedBody.json().get("rate");
                if (jsonRate != null) {
                    rateParam = String.valueOf(jsonRate);
                }
            }
            log.info("rateParam: " + rateParam + " [File: ExchangeRateServlet.java]");
            if (!Validation.isStringValid(rateParam)) {
                log.warning("ExchangeRateServlet: Invalid rate parameter: " + rateParam + " [File: ExchangeRateServlet.java]");
                throw new IllegalArgumentException("Invalid rate parameter");
            }

            Validation.isStringConvertableToBigDecimal(rateParam);
            log.info("rateParam: " + rateParam + " [File: ExchangeRateServlet.java]");
            BigDecimal newRate = new BigDecimal(rateParam);

            if (Validation.isZeroOrNegative(newRate)) {
                log.warning("ExchangeRateServlet: Invalid rate value: " + newRate + " [File: ExchangeRateServlet.java]");
                throw new IllegalArgumentException("Invalid rate value");
            }

            int baseCurrencyId = 0;
            int targetCurrencyId = 0;
            baseCurrencyId = currenciesDbConnector.findByCode(baseCurrencyCode).getId();
            targetCurrencyId = currenciesDbConnector.findByCode(targetCurrencyCode).getId();

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrencyId, targetCurrencyId);
            if (existing == null) {
                throw new NullPointerException("Exchange rate not found");
            }

            ExchangeRate updated = exchangeRatesDbConnector.update(baseCurrencyId, targetCurrencyId, newRate);
            if (updated == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            log.info("CurrencyExchange rate updated:" + updated + " [File: ExchangeRateServlet.java]");
            DTOExchangeRate dto = new DTOExchangeRate(updated.getId(), currenciesDbConnector.findById(baseCurrencyId), currenciesDbConnector.findById(targetCurrencyId), updated.getRate().setScale(2, RoundingMode.HALF_UP));
            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (IllegalArgumentException e) {
            log.info("ExchangeRate servlet IllegalArgumentException(doPATCH): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (NullPointerException e) {
            log.info("ExchangeRate servlet NullPointerException(doPATCH): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Null pointer exception");

        } catch (Exception e) {
            log.info("ExchangeRate servlet unexpected Exception(doPATCH): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            throw new ServletException("Error updating exchange rate", e);
        }
    }
}
