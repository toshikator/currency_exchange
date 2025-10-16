package third_project.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.Currency;
import third_project.DbConnection.CurrenciesDB;

import java.io.IOException;
import java.io.PrintWriter;

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
//            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
//            String targetCurrencyCode = request.getParameter("targetCurrencyCode");

//            if (baseCurrencyCode == null || targetCurrencyCode == null) {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency codes are required");
//                return;
//            }

            try {
//                if (baseCurrencyCode.equals(targetCurrencyCode)) throw new IllegalArgumentException();
//                Currency baseCurrency = CurrenciesDB.selectOne(baseCurrencyCode);
//                Currency targetCurrency = CurrenciesDB.selectOne(targetCurrencyCode);

                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
//                out.println("<h2>Exchange Rate Information</h2>");
//                out.println("<p>Base Currency: " + baseCurrency.getCode() + " - " + baseCurrency.getFullName() + "</p>");
//                out.println("<p>Target Currency: " + targetCurrency.getCode() + " - " + targetCurrency.getFullName() + "</p>");
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Base currency code must be different from target currency code");
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid currency code or unfound currency code");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String baseCurrencyCode = request.getParameter("baseCurrencyCode");
            String targetCurrencyCode = request.getParameter("targetCurrencyCode ");
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
