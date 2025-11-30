package third_project.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.entities.Currency;
import third_project.DbConnection.CurrenciesDbConnector;
import third_project.service.Validation;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "currency", value = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrenciesDbConnector currenciesDbConnector;
    private Validation validator;
    private DataSource ds;

    public CurrencyServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        currenciesDbConnector = (CurrenciesDbConnector) getServletContext().getAttribute("currenciesDbConnector");
        if (currenciesDbConnector == null)
            throw new ServletException("currenciesDbConnector not found in ServletContext");

        ds = (DataSource) getServletContext().getAttribute("datasource");
        // ds may be optional for this servlet; don't fail deployment if it's not required here
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String pathInfo = request.getPathInfo();
            if (!Validation.isStringValid(pathInfo)) {
                throw new IllegalArgumentException("Invalid pathInfo");
            }
            Currency currency;

            currency = Validation.isCurrencyExist(request.getPathInfo().substring(1)) ? currenciesDbConnector.findByCode(pathInfo.substring(1)) : null;
            if (currency == null) {
                throw new ServletException("Currency not found");
            }
            PrintWriter out = response.getWriter();
            out.println(currency);
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (IllegalArgumentException e) {
            System.err.println("invalid pathInfo");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServletException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
