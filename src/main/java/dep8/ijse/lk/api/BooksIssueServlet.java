package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.BookDTO;
import dep8.ijse.lk.dto.IssueBookDTO;
import dep8.ijse.lk.exception.ValidationException;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

@WebServlet(name = "BooksIssueServlet", urlPatterns = {"/issue","/issue/"})
public class BooksIssueServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    public volatile DataSource pool;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM Issues");
            ResultSet rst = stm.executeQuery();
            Jsonb jsonb = JsonbBuilder.create();
            ArrayList<IssueBookDTO> issues = new ArrayList<>();
            while (rst.next()) {
                issues.add(new IssueBookDTO(rst.getString("issueId"), rst.getString("bookId")
                        , rst.getString("memberId"), rst.getString("datetime")));
            }
            response.setContentType("application/json");
            jsonb.toJson(issues, response.getWriter());
            issues.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to fetch data");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        IssueBookDTO issue = null;
        try {
            issue = jsonb.fromJson(request.getReader(), IssueBookDTO.class);
            if (!issue.getIssueId().matches("IS[0-9][0-9][0-9]")) {
                throw new ValidationException("Invalid Issue ID");
            }
        } catch (ValidationException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            response.getWriter().write(e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO Issues(issueId, bookId, memberId, dateTime) VALUES (?,?,?,?)");
            stm.setString(1, issue.getIssueId());
            stm.setString(2, issue.getBookId());
            stm.setString(3, issue.getMemberId());
            stm.setString(4, String.valueOf(LocalDateTime.now()));
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to Issue the book!");
            }
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Successfully Issue the book!");

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().write("Failed to Issue the book!");
        }
    }
}
