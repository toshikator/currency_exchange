package third_project.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.Currency;
import third_project.DTOExchangeRate;
import third_project.DbConnection.CurrenciesDB;
import third_project.DbConnection.ExchangeRatesDB;
import third_project.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "exchangeRates", value = "/exchangeRates")
public class ExchangeRates extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ExchangeRates() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
//            List<ExchangeRate> exchangeRates = ExchangeRatesDB.selectAll();
            List<DTOExchangeRate> exchangeRates = ExchangeRatesDB.selectAll().parallelStream().map(DTOExchangeRate::new).collect(Collectors.toList());
            if (exchangeRates == null || exchangeRates.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            mapper.writeValue(response.getWriter(), exchangeRates);
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);


        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode");
            try {
                if (baseCurrencyCode.equals(targetCurrencyCode)) throw new IllegalArgumentException();
                Currency baseCurrency = CurrenciesDB.selectOne(baseCurrencyCode);
                Currency targetCurrency = CurrenciesDB.selectOne(targetCurrencyCode);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Base currency code must be different from target currency code");
                return;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid currency code or unfound currency code");
                return;
            }
            double rate = Double.parseDouble(request.getParameter("rate"));
            System.out.println(rate);
            if (rate <= 0) throw new NumberFormatException();
            if (baseCurrencyCode == null || targetCurrencyCode == null)
                throw new NullPointerException();

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<h2>Received Data</h2>");
            out.println("<p>baseCurrencyCode: " + baseCurrencyCode + "</p>");
            out.println("<p>targetCurrencyCode: " + targetCurrencyCode + "</p>");
            out.println("<p>rate: " + rate + "</p>");
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rate format or value");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred");
        }
    }
}
