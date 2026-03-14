package third_project.servlet;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchange;
import third_project.entities.ExchangeRate;
import third_project.service.Validation;

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

            if (currenciesDbConnector.findByCode(from) == null) {
                log.warning("ExchangeServlet: base currency not found: from=" + from + " [File: ExchangeServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "base currency not found");
                return;
            }
            if (currenciesDbConnector.findByCode(to) == null) {
                log.warning("ExchangeServlet: target currency not found: to=" + to + " [File: ExchangeServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "target currency not found");
                return;
            }
            ExchangeRate rate;
            DTOExchange result;
            if (Validation.isExchangeRateExist(exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(),
                    currenciesDbConnector.findByCode(to).getId()))) {
                rate = exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(),
                        currenciesDbConnector.findByCode(to).getId());
                BigDecimal convertedAmount = amount.multiply(rate.getRate());
                convertedAmount = convertedAmount.setScale(2, RoundingMode.HALF_UP);
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to),
                        rate.getRate(), amount, convertedAmount);
            } else if (Validation.isExchangeRateExist(exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId()))) {
                rate = exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId());
                BigDecimal convertedAmount = amount.multiply(new BigDecimal(1).divide(rate.getRate(), 6, RoundingMode.HALF_UP));
                convertedAmount = convertedAmount.setScale(2, RoundingMode.HALF_UP);
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), rate.getRate(), amount, convertedAmount);
            } else if (Validation.isExchangeRateExist(exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode("USD").getId())) &&
                    (Validation.isExchangeRateExist(exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode("USD").getId(), currenciesDbConnector.findByCode(to).getId())))) {
                ExchangeRate firstRate = exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode("USD").getId());
                ExchangeRate secondRate = exchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode("USD").getId(), currenciesDbConnector.findByCode(to).getId());
                BigDecimal convertedAmount = amount.multiply(firstRate.getRate()).divide(secondRate.getRate(), 6, RoundingMode.HALF_UP);
                convertedAmount = convertedAmount.setScale(2, RoundingMode.HALF_UP);
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), firstRate.getRate().multiply(secondRate.getRate()), amount, convertedAmount);

            } else {
                //                throw new Exception("Exchange servlet: no currency exchange rate");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "no currency exchange rate");
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK, result);

        } catch (Exception e) {
            log.info("ExchangeServlet Exception(doGET) :" + e.getMessage() + " [File: ExchangeServlet.java]");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Currency not found or exchange rate not found or shit happened");
        }


    }

}
