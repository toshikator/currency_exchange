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
            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);
            ExchangeRate rate = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            DTOExchangeRate dto = new DTOExchangeRate(rate.getId(), baseCurrency, targetCurrency, rate.getRate().setScale(2, RoundingMode.HALF_UP));

            //            response.getWriter().println(new ObjectMapper().writeValueAsString(dto));
            //            response.setStatus(HttpServletResponse.SC_OK);
            writeJson(response, HttpServletResponse.SC_OK, dto);
        } catch (NullPointerException e) {
            log.info("ExchangeRate servlet NullPointerException(doGET_1): " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            log.info("ExchangeRate servlet unexpected Exception(doGET_2): " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.info("loloL " + e);
            throw new ServletException("Error processing exchange rate request", e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        log.info("ExchangeRate servlet doPATCH");
        try {
            String pathInfo = request.getPathInfo().toUpperCase();
            log.info("pathInfo: " + pathInfo);

            if (!Validation.isPatchRequestValid(pathInfo)) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo);
                throw new IllegalArgumentException("Invalid pathInfo");
            }

            String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            String targetCurrencyCode = pathInfo.substring(4).toUpperCase();
            log.info("baseCurrencyCode: " + baseCurrencyCode + " targetCurrencyCode: " + targetCurrencyCode);

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
            log.info("rateParam: " + rateParam);
            if (!Validation.isStringValid(rateParam)) {
                log.info("String is invalid");
                log.warning("ExchangeRateServlet: Invalid rate parameter: " + rateParam);
                throw new IllegalArgumentException("Invalid rate parameter");
            }

            Validation.isStringConvertableToBigDecimal(rateParam);
            log.info("rateParam: " + rateParam);
            BigDecimal newRate = new BigDecimal(rateParam);

            if (Validation.isZeroOrNegative(newRate)) {
                log.warning("ExchangeRateServlet: Invalid rate value: " + newRate);
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
            log.info("ExchangeRate updated:" + updated);
            log.info("CurrencyExchange rate updated:" + updated);
            DTOExchangeRate dto = new DTOExchangeRate(updated.getId(), currenciesDbConnector.findById(baseCurrencyId), currenciesDbConnector.findById(targetCurrencyId), updated.getRate().setScale(2, RoundingMode.HALF_UP));
            //            response.getWriter().println(new ObjectMapper().writeValueAsString(dto));
            //            response.setStatus(HttpServletResponse.SC_OK);
            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            log.info("ExchangeRate servlet IllegalArgumentException(doPATCH): " + e.getMessage());
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            log.info(e.getMessage());
            log.info("ExchangeRate servlet NullPointerException(doPATCH): " + e.getMessage());
            response.getWriter().println(e.getMessage() + " Null pointer exception");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        } catch (Exception e) {
            log.info(e.getMessage());
            log.info("ExchangeRate servlet unexpected Exception(doPATCH): " + e.getMessage());
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException("Error updating exchange rate", e);
        }
    }
}
