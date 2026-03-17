package third_project.servlet;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchange;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet(name = "exchange", value = "/exchange")
public class ExchangeServlet extends BaseServlet {


    public ExchangeServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String from = request.getParameter("from");
            String to = request.getParameter("to");
            String amountStr = request.getParameter("amount");
            BigDecimal amount = new BigDecimal(amountStr);

            if (currenciesDbConnector.findByCode(from).isEmpty()) {
                log.warning("ExchangeServlet: base currency not found: from=" + from + " [File: ExchangeServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "base currency not found");
                return;
            }
            if (currenciesDbConnector.findByCode(to).isEmpty()) {
                log.warning("ExchangeServlet: target currency not found: to=" + to + " [File: ExchangeServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "target currency not found");
                return;
            }
            ExchangeRate rate;
            DTOExchange result;

            Currency baseCurrency = currenciesDbConnector.findByCode(from).orElseThrow(() -> new IllegalStateException("Somehow base currency wasn't found"));
            Currency targetCurrency = currenciesDbConnector.findByCode(to).orElseThrow(() -> new IllegalStateException("Somehow target currency wasn't found"));

            ExchangeRate directRate = exchangeRatesDbConnector.findRate(baseCurrency.getId(), targetCurrency.getId()).orElse(null);
            if (directRate != null) {
                rate = directRate;
                result = new DTOExchange(baseCurrency, targetCurrency, rate.getRate(), amount, amount.multiply(rate.getRate()));
                writeJson(response, HttpServletResponse.SC_OK, result);
                return;
            }

            ExchangeRate inverseRate = exchangeRatesDbConnector.findRate(targetCurrency.getId(), baseCurrency.getId()).orElse(null);
            if (inverseRate != null) {
                rate = inverseRate;
                result = new DTOExchange(baseCurrency, targetCurrency, rate.getRate(), amount, amount.divide(rate.getRate(), 6, RoundingMode.HALF_UP));
                writeJson(response, HttpServletResponse.SC_OK, result);
                return;
            }

            Currency usdCurrency = currenciesDbConnector.findByCode("USD").orElseThrow(() -> new IllegalStateException("Somehow there isn't currency for USD"));
            ExchangeRate baseToUSD = exchangeRatesDbConnector.findRate(baseCurrency.getId(), usdCurrency.getId())
                    .orElseThrow(() -> new IllegalStateException("Somehow base currency to USD exchange rate wasn't found"));
            ExchangeRate USDtoTarget = exchangeRatesDbConnector.findRate(usdCurrency.getId(), targetCurrency.getId())
                    .orElseThrow(() -> new IllegalStateException("Somehow USD to target currency exchange rate wasn't found"));

            BigDecimal convertedAmount = amount.multiply(baseToUSD.getRate()).divide(USDtoTarget.getRate(), 6, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            result = new DTOExchange(baseCurrency, targetCurrency, baseToUSD.getRate().multiply(USDtoTarget.getRate()), amount, convertedAmount);
            writeJson(response, HttpServletResponse.SC_OK, result);

        } catch (IllegalStateException ise) {
            log.info("ExchangeServlet Exception(doGET) :" + ise.getMessage() + " [File: ExchangeServlet.java]");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, ise.getMessage());
        } catch (Exception e) {
            log.info("ExchangeServlet Exception(doGET) :" + e.getMessage() + " [File: ExchangeServlet.java]");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Currency not found or exchange rate not found or shit happened" + e.getMessage());
        }


    }

}
