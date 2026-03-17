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
            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode).orElseThrow(() -> new IllegalStateException("Somehow base currency wasn't found"));
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode).orElseThrow(() -> new IllegalStateException("Somehow target currency wasn't found"));

            ExchangeRate rate = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId()).orElseThrow(() -> new IllegalStateException("Somehow exchange rate wasn't found"));

            DTOExchangeRate dto = new DTOExchangeRate(rate.getId(), baseCurrency, targetCurrency, rate.getRate().setScale(2, RoundingMode.HALF_UP));

            writeJson(response, HttpServletResponse.SC_OK, dto);
        } catch (IllegalStateException ise) {
            log.info("ExchangeRate servlet IllegalStateException(doGET): " + ise.getMessage() + " [File: ExchangeRateServlet.java]");
            writeError(response, HttpServletResponse.SC_NOT_FOUND, ise.getMessage());
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

            if (!Validation.isPatchRequestValid(pathInfo)) {
                log.warning("ExchangeRateServlet: Invalid pathInfo: " + pathInfo + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }

            String baseCurrencyCode = pathInfo.substring(1, 4).toUpperCase();
            String targetCurrencyCode = pathInfo.substring(4).toUpperCase();

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
            if (!Validation.isStringConvertableToBigDecimalRate(rateParam)) {
                log.warning("ExchangeRateServlet: Invalid rate parameter: " + rateParam + " [File: ExchangeRateServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid rate parameter");
                return;
            }


            assert rateParam != null;
            BigDecimal newRate = new BigDecimal(rateParam);


            int baseCurrencyId = currenciesDbConnector.findByCode(baseCurrencyCode)
                    .orElseThrow(() -> new IllegalStateException("Somehow base currency wasn't found")).getId();
            int targetCurrencyId = currenciesDbConnector.findByCode(targetCurrencyCode)
                    .orElseThrow(() -> new IllegalStateException("Somehow target currency wasn't found")).getId();

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrencyId, targetCurrencyId).orElseThrow(() -> new IllegalStateException("Somehow exchange rate wasn't found"));

            ExchangeRate updated = exchangeRatesDbConnector.update(baseCurrencyId, targetCurrencyId, newRate).orElseThrow(() -> new IllegalStateException("Exchange rate wasn't updated"));
            DTOExchangeRate dto = new DTOExchangeRate(updated.getId()
                    , currenciesDbConnector.findById(baseCurrencyId).orElseThrow(() -> new Exception("Somehow base currency wasn't found"))
                    , currenciesDbConnector.findById(targetCurrencyId).orElseThrow(() -> new Exception("Somehow target currency wasn't found"))
                    , updated.getRate().setScale(2, RoundingMode.HALF_UP));
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
