package third_project.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchangeRate;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.PatchBodyParser;
import third_project.service.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;


@WebServlet(name = "exchangeRate", value = "/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {


    public ExchangeRateServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() != 7) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }
            String path = pathInfo.substring(1);
            String baseCurrencyCode = path.substring(0, 3).toUpperCase();
            String targetCurrencyCode = path.substring(3, 6).toUpperCase();
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
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String pathInfo = request.getPathInfo().toUpperCase();
            //            log.info("pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");

            if (!Validation.isPatchRequestValid(pathInfo)) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }

            String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            String targetCurrencyCode = pathInfo.substring(4).toUpperCase();
            //            log.info("baseCurrencyCode: " + baseCurrencyCode + " targetCurrencyCode: " + targetCurrencyCode + " [File: ExchangeRateServlet.java]");

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
            //            log.info("rateParam: " + rateParam + " [File: ExchangeRateServlet.java]");
            if (!Validation.isStringConvertableToBigDecimalRate(rateParam)) {
                log.warning("ExchangeRateServlet: Invalid rate parameter: " + rateParam + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid rate parameter");
                return;
            }

            log.info("rateParam: " + rateParam + " [File: ExchangeRateServlet.java]");
            BigDecimal newRate = new BigDecimal(rateParam);

            //            if (Validation.isZeroOrNegative(newRate)) {
            //                log.warning("ExchangeRateServlet: Invalid rate value: " + newRate + " [File: ExchangeRateServlet.java]");
            //                throw new IllegalArgumentException("Invalid rate value");
            //            }

            int baseCurrencyId = 0;
            int targetCurrencyId = 0;
            baseCurrencyId = currenciesDbConnector.findByCode(baseCurrencyCode).getId();
            targetCurrencyId = currenciesDbConnector.findByCode(targetCurrencyCode).getId();

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrencyId, targetCurrencyId);
            if (existing == null) {
                log.info("ExchangeRate servlet: exchange rate doesn't exist" + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Exchange rate doesn't exist");
                return;
            }

            ExchangeRate updated = exchangeRatesDbConnector.update(baseCurrencyId, targetCurrencyId, newRate);
            if (updated == null) {
                log.info("ExchangeRate servlet exchange rate wasn't updated!(doPATCH) " + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Exchange rate wasn't updated");
                return;
            }
            log.info("CurrencyExchange rate updated:" + updated + " [File: ExchangeRateServlet.java]");
            DTOExchangeRate dto = new DTOExchangeRate(updated.getId(), currenciesDbConnector.findById(baseCurrencyId), currenciesDbConnector.findById(targetCurrencyId), updated.getRate().setScale(2, RoundingMode.HALF_UP));
            writeJson(response, HttpServletResponse.SC_OK, dto);

        } catch (IllegalArgumentException e) {
            log.info("ExchangeRate servlet IllegalArgumentException(doPATCH): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.info("ExchangeRate servlet unexpected Exception(doPATCH): " + e.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
