package third_project.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "test", value = "/test")
public class TestServlet extends HttpServlet {


    private DataSource ds;

    @Override
    public void init() throws ServletException {
        ds = (DataSource) getServletContext().getAttribute("datasource");
        if (ds == null) throw new ServletException("DataSource not found in ServletContext");
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.getWriter().write("PATCH OK");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain; charset=UTF-8");

        String sql = "SELECT employee_id, first_name, last_name FROM hr.employees FETCH FIRST 10 ROWS ONLY";
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // (опционально) таймаут на запрос, чтобы не висеть вечно:
            ps.setQueryTimeout(10); // сек

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resp.getWriter().println(
                            rs.getInt("employee_id") + " | " +
                                    rs.getString("first_name") + " " + rs.getString("last_name"));
                }
            }
        } catch (SQLException e) {
            getServletContext().log("DB error", e);
            resp.setStatus(500);
            resp.getWriter().println("Internal error");
        }
    }


}
