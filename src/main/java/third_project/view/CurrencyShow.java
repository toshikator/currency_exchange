package third_project.view;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import third_project.Currency;
import third_project.DbConnection.CurrenciesDB;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

@WebServlet(name = "currency", value = "/currency/*")
public class CurrencyShow extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Currency currency = CurrenciesDB.selectOne(pathInfo.substring(1));
//            Currency currency = CurrenciesDB.selectOne(id);


            PrintWriter out = response.getWriter();
            out.println(currency);
            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().println("Hello WorldD!");
//            response.getWriter().println(pathInfo);
//            response.getWriter().println("\n\n");
//            response.getWriter().println(pathInfo.substring(1));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }
}
