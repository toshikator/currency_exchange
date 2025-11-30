package third_project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.entities.Currency;
import third_project.service.Validation;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "currenciesList", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrenciesDbConnector currenciesDbConnector;

    public CurrenciesServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null) {
            throw new ServletException("currenciesDbConnector not found in ServletContext");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String name = request.getParameter("name");
            String code = request.getParameter("code");
            String sign = request.getParameter("sign");

            if (!Validation.areThreeStringsValid(name, code, sign)) {
                System.err.println("invalid parameters");
                throw new IllegalArgumentException("Invalid parameters");
            }
            if (currenciesDbConnector.findByCode(code) != null) {
                System.err.println("currency with such code already exists");
                throw new IllegalStateException("Currency with such code already exists");
            }
            Currency currency = currenciesDbConnector.insert(code, name, sign);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), currency);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("invalid parameters");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException e) {
            System.err.println("currency with such code already exists");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (Exception e) {
            System.out.println("error by currencies servlet, cause: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Currency> currencies = currenciesDbConnector.getAllCurrencies();
            if (!Validation.isListValid(currencies)) {
                System.out.println("error by currencies servlet, cause: currencies list is null or empty");
                throw new IllegalStateException("currencies list is null or empty");
            }

            mapper.writeValue(response.getWriter(), currencies);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            System.out.println("error by currencies servlet, cause: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}


