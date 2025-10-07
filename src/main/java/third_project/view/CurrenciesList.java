package third_project.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import third_project.DbConnection.CurrenciesDB;
import third_project.Currency;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "currenciesList", value = "/currencies")
public class CurrenciesList extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Currency> currencies = CurrenciesDB.select();
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
