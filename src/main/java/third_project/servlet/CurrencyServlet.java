package third_project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.entities.Currency;
import third_project.service.Validation;

import java.io.IOException;

@WebServlet(name = "currency", value = "/currency/*")
public class CurrencyServlet extends BaseServlet {


    public CurrencyServlet() {
        super();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String pathInfo = request.getPathInfo();
            if (!Validation.isStringValid(pathInfo)) {
                log.warning("CurrencyServlet: Invalid pathInfo: " + pathInfo + " [File: CurrencyServlet.java]");
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
                return;
            }
            Currency currency;

            currency = Validation.isCurrencyExist(request.getPathInfo().substring(1), currenciesDbConnector)
                    ? currenciesDbConnector.findByCode(pathInfo.substring(1)).orElseThrow(() -> new IllegalStateException("Somehow currency wasn't found")) : null;
            if (currency == null) {
                log.warning("CurrencyServlet: Currency not found for pathInfo=" + pathInfo + " [File: CurrencyServlet.java]");
                writeError(response, HttpServletResponse.SC_NOT_FOUND, "Currency not found");
                return;
            }

            writeJson(response, HttpServletResponse.SC_OK, currency);

            //        } catch (IllegalArgumentException e) {
            //            log.info("Currency servlet IllegalArgumentException(invalid pathInfo): " + e.getMessage() + " [File: CurrencyServlet.java]");
            //            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid pathInfo");
        } catch (Exception e) {
            log.info("Currency servlet unexpected Exception: " + e.getMessage() + " [File: CurrencyServlet.java]");
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
