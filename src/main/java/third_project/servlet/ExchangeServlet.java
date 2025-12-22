package third_project.servlet;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.dto.DTOExchange;
import third_project.dto.DTOExchangeRate;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.DbConnection.ExchangeRatesDbConnector;
import third_project.entities.Currency;
import third_project.entities.ExchangeRate;
import third_project.service.Validation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet(name = "exchange", value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private CurrenciesDbConnector currenciesDbConnector;
    private ExchangeRatesDbConnector exchangeRatesDbConnector;

    public ExchangeServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null)
            throw new ServletException("currenciesDbConnector not found in ServletContext");
        exchangeRatesDbConnector = (ExchangeRatesDbConnector) getServletContext().getAttribute("exchangeRatesDbConnector");
        if (exchangeRatesDbConnector == null)
            throw new ServletException("exchangeRatesDbConnector not found in ServletContext");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String from = request.getParameter("from");
            String to = request.getParameter("to");
            String amountStr = request.getParameter("amount");
            BigDecimal amount = new BigDecimal(amountStr);

            if (currenciesDbConnector.findByCode(from) == null) {
                throw new IllegalArgumentException("base currency not found");
            }
            if (currenciesDbConnector.findByCode(to) == null) {
                throw new IllegalArgumentException("target currency not found");
            }
            ExchangeRate rate;
            DTOExchange result;
            if (Validation.isExchangeRateExist(ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode(to).getId()))) {
                rate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode(to).getId());
                BigDecimal convertedAmount = amount.multiply(rate.getRate());
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), rate.getRate(), amount, convertedAmount);
                System.out.println(result);
                response.getWriter().write(result.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else if (Validation.isExchangeRateExist(ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId()))) {
                rate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId());
                BigDecimal convertedAmount = amount.multiply(new BigDecimal(1).divide(rate.getRate(), 6, RoundingMode.HALF_UP));
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), rate.getRate(), amount, convertedAmount);
                System.out.println(result);
                response.getWriter().write(result.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            } else if (Validation.isExchangeRateExist(ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode("USD").getId())) &&
                    (Validation.isExchangeRateExist(ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode("USD").getId(), currenciesDbConnector.findByCode(to).getId())))) {
                ExchangeRate firstRate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode("USD").getId());
                ExchangeRate secondRate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode("USD").getId(), currenciesDbConnector.findByCode(to).getId());
                BigDecimal convertedAmount = amount.multiply(firstRate.getRate()).divide(secondRate.getRate(), 6, RoundingMode.HALF_UP);
                result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), firstRate.getRate().multiply(secondRate.getRate()), amount, convertedAmount);
                System.out.println(result);
                response.getWriter().write(result.toString());
                response.setStatus(HttpServletResponse.SC_OK);

            } else {
                throw new Exception("Exchange servlet: no currency exchange rate");
            }


//            ExchangeRate rate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode(to).getId());
//            if (rate == null) {
//                errMsg.append("Exchange rate for ").append(from).append(" to ").append(to).append(" not found(straight course)");
//                System.out.println("error on convertation");
//                System.out.println("no currency exchange rate");
//            } else {
//                BigDecimal convertedAmount = amount.multiply(rate.getRate());
//                DTOExchange result = new DTOExchange(currenciesDbConnector.findByCode(from), currenciesDbConnector.findByCode(to), rate.getRate(), amount, convertedAmount);
//                System.out.println(result);
//                response.getWriter().write(result.toString());
//                response.setStatus(HttpServletResponse.SC_OK);
//            }


//            rate = ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId());
//            if (ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(to).getId(), currenciesDbConnector.findByCode(from).getId()) == null) {
//                errMsg.append("Exchange rate for ").append(from).append(" to ").append(to).append(" not found(reverse course)");
//            }
//            if (ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode(from).getId(), currenciesDbConnector.findByCode("USD").getId()) == null
//                    && ExchangeRatesDbConnector.findRate(currenciesDbConnector.findByCode("USD").getId(), currenciesDbConnector.findByCode(to).getId()) == null) {
//                errMsg.append("Exchange rate for ").append(from).append(" to ").append(to).append(" not found(cross USD course)");
//            }


        } catch (Exception e) {
            System.out.println("Exchange servlet: Exception in doGet");
            System.err.println(e);
            response.getWriter().write("currency did not found" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }


    }

}
