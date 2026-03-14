package third_project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.entities.Currency;
import third_project.service.Validation;

import java.io.IOException;
import java.util.List;

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
                log.warning("name = " + name + ", code = " + code + ", sign = " + sign + " [File: CurrenciesServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
                return;
            }
            if (currenciesDbConnector.findByCode(code) != null) {
                log.warning("currency with such code already exists [File: CurrenciesServlet.java]");
                writeError(response, HttpServletResponse.SC_CONFLICT, "Currency with such code already exists");
                return;
            }
            Currency currency = currenciesDbConnector.insert(code, name, sign);

            writeJson(response, HttpServletResponse.SC_CREATED, currency);

        } catch (Exception e) {

            log.info("CurrenciesServlet unexpected(POST) Exception: " + e.getMessage() + " [File: CurrenciesServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Currency> currencies = currenciesDbConnector.getAllCurrencies();
            writeJson(response, HttpServletResponse.SC_OK, currencies);
        } catch (Exception e) {
            log.warning("CurrenciesServlet unexpected(GET) Exception: " + e.getMessage() + " [File: CurrenciesServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

}


