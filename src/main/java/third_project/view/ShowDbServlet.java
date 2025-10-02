package third_project.view;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletResponse;
import third_project.CurrenciesDB;
import third_project.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "showDbServlet", value = "/show-db")
public class ShowDbServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CurrenciesDB currenciesDB;

    public ShowDbServlet() {
        super();
        this.currenciesDB = new CurrenciesDB();
    }

    public void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        try {
            // Fetch data first to decide proper status code
            List<Currency> currencies = CurrenciesDB.select();

            if (currencies == null || currencies.isEmpty()) {
                // No content to show
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            // Success with content
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
            // Internal server error
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
