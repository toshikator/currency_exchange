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
import java.util.logging.Logger;

@WebServlet(name = "currenciesList", value = "/currencies")
public class CurrenciesServlet extends BaseServlet {

    public CurrenciesServlet() {
        super();
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
                log.info("invalid parameters");
                throw new IllegalArgumentException("Invalid parameters");
            }
            if (currenciesDbConnector.findByCode(code) != null) {
                log.info("currency with such code already exists");
                throw new IllegalStateException("Currency with such code already exists");
            }
            Currency currency = currenciesDbConnector.insert(code, name, sign);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), currency);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IllegalArgumentException e) {

            log.info("CurrenciesServlet IllegalArgumentException(POST)(invalid parameters): " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException e) {

            log.info("CurrenciesServlet IllegalStateException(POST)(currency with such code already exists): " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (Exception e) {

            log.info("CurrenciesServlet unexpected(POST) Exception: " + e.getMessage());
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
            

            mapper.writeValue(response.getWriter(), currencies);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {

            log.info("CurrenciesServlet unexpected(GET) Exception: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}


