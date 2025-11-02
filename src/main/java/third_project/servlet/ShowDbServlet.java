package third_project.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import third_project.DbConnection.CurrenciesDB;
import third_project.entities.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "showDbServlet", value = "/show-db")
public class ShowDbServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ShowDbServlet() {
        super();
        CurrenciesDB currenciesDB = new CurrenciesDB();
    }

    public void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        try {
            List<Currency> currencies = CurrenciesDB.getAllCurrencies();

            if (currencies == null || currencies.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println("<html><body>");
            out.println("<h2><a href='index.jsp'>Home</a></h2>");
            for (Currency currency : currencies) {
                out.println("<div>" + currency.toString() + "</div>");
            }
            out.println("<h2><a href='index.jsp'>Home</a></h2>");
            out.println("</body></html>");
        } catch (Exception e) {
            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                PrintWriter out = response.getWriter();
                out.println("<html><body>");
                out.println("<h2>Internal Server Error</h2>");
                out.println("<p>" + e.getMessage() + "</p>");
                out.println("</body></html>");
            } catch (IllegalStateException ignored) {
                // response might be already committed
            }
        }
    }
}
