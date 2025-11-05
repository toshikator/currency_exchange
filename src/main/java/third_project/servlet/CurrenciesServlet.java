package third_project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.entities.Currency;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "currenciesList", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrenciesDbConnector currenciesDbConnector;

    public CurrenciesServlet() {
        this.currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String name = request.getParameter("name");
            String code = request.getParameter("code");
            String sign = request.getParameter("sign");

            if (name == null || code == null || sign == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (currenciesDbConnector.findByCode(code) != null) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
            Currency currency = currenciesDbConnector.insert(code, name, sign);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), currency);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Currency> currencies = currenciesDbConnector.getAllCurrencies();
            if (currencies == null || currencies.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            mapper.writeValue(response.getWriter(), currencies);
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}


