package third_project.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchangeRate;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {


    public ExchangeRatesServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            List<DTOExchangeRate> exchangeRates = exchangeRatesDbConnector.selectAll().parallelStream().map((exchangeRate) -> {
                DTOExchangeRate result = new DTOExchangeRate();
                result.setId(exchangeRate.getId());
                result.setBaseCurrency(currenciesDbConnector.findById(exchangeRate.getBaseCurrencyId()));
                result.setTargetCurrency(currenciesDbConnector.findById(exchangeRate.getTargetCurrencyId()));
                result.setRate(exchangeRate.getRate().setScale(2, RoundingMode.HALF_UP));
                return result;
            }).collect(Collectors.toList());
            if (exchangeRates.isEmpty()) {
                log.warning("ExchangeRatesServlet (doGet): empty dataset of DTOs: " + " [File: ExchangeRatesServlet.java]");
                writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "empty dataset of DTOs");
            }
            writeJson(response, HttpServletResponse.SC_OK, exchangeRates);

        } catch (Exception e) {
            log.warning("ExchangeRatesServlet (doGet): servlet global error: " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ExchangeRatesServlet General exception");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
            String rateParam = request.getParameter("rate");

            if (!Validation.isStringValid(baseCurrencyCode)
                    || !Validation.isStringValid(targetCurrencyCode)
                    || !Validation.isStringConvertableToBigDecimalRate(rateParam)) {
                log.warning("ExchangeRatesServlet: invalid parameters: base=" +
                        baseCurrencyCode + ", " +
                        "target=" + targetCurrencyCode + ", rate=" + rateParam + " [File: ExchangeRatesServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameters input");
            }

            baseCurrencyCode = baseCurrencyCode.toUpperCase();
            targetCurrencyCode = targetCurrencyCode.toUpperCase();

            if (baseCurrencyCode.equals(targetCurrencyCode)) {
                log.warning("ExchangeRatesServlet: baseCurrencyCode and targetCurrencyCode must be different: "
                        + baseCurrencyCode + " [File: ExchangeRatesServlet.java]");
                //                throw new IllegalStateException("baseCurrencyCode and targetCurrencyCode must be different");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "base currency and target currency should be different");
            }

            Currency baseCurrency = currenciesDbConnector.findByCode(baseCurrencyCode);
            Currency targetCurrency = currenciesDbConnector.findByCode(targetCurrencyCode);

            if (!Validation.isCurrencyValid(baseCurrency) || !Validation.isCurrencyValid(targetCurrency)) {
                log.warning("ExchangeRatesServlet: invalid currency code(s): base=" + baseCurrencyCode + ", target="
                        + targetCurrencyCode + " [File: ExchangeRatesServlet.java]");
                //                throw new IllegalArgumentException("invalid currency ");
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "invalid currency or currency doesn't presented");
            }

            BigDecimal rate;
            rate = new BigDecimal(rateParam);

            ExchangeRate existing = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId());
            if (existing != null) {
                log.info("exchange rate already exists [File: ExchangeRatesServlet.java]");
                writeError(response, HttpServletResponse.SC_CONFLICT, "exchange rate already exists");
                return;
            }

            ExchangeRate created = exchangeRatesDbConnector.insert(baseCurrencyCode, targetCurrencyCode, rate);
            DTOExchangeRate dto = new DTOExchangeRate(created.getId(), baseCurrency, targetCurrency, rate);
            writeJson(response, HttpServletResponse.SC_CREATED, dto);

        } catch (Exception e) {
            log.info("ExchangeRatesServlet General exception(doPOST): " + e.getMessage() + " [File: ExchangeRatesServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ExchangeRatesServlet General exception");
        }
    }
}
