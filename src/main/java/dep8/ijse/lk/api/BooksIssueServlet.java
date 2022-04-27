package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.BookDTO;
import dep8.ijse.lk.dto.IssueBookDTO;
import dep8.ijse.lk.exception.ValidationException;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

@WebServlet(name = "BooksIssueServlet", urlPatterns = {"/issues","/issues/"})
public class BooksIssueServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    public volatile DataSource pool;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getPathInfo()!=null && !request.getPathInfo().equals("/")){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String query = request.getParameter("q");
        query = "%"  + ((query == null) ? "": query) + "%";

        boolean paginations = request.getParameter("page")!=null && request.getParameter("size")!=null;
        String sql=null;
        if (paginations){
            sql="SELECT * FROM Issues WHERE bookId LIKE ? OR memberId LIKE ? LIMIT ? OFFSET ?";
        }else {
            sql="SELECT * FROM Issues WHERE bookId LIKE ? OR memberId LIKE ?";
        }

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1,query);
            stm.setString(2,query);
            if (paginations){
                int page = Integer.parseInt(request.getParameter("page"));
                int size = Integer.parseInt(request.getParameter("size"));
                stm.setInt(3,page);
                stm.setInt(4,(page-1)*size);
            }
            ResultSet rst = stm.executeQuery();
            PreparedStatement pst = connection.prepareStatement("SELECT count(*) FROM Issues WHERE bookId LIKE ? OR memberId LIKE ?");
            pst.setString(1,query);
            pst.setString(2,query);
            ResultSet rest = pst.executeQuery();
            if (rest.next()){
                response.setHeader("X-Count",rest.getString(1));
            }else {
                response.setHeader("X-Count","0");
            }

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
        if (request.getContentType()==null || !request.getContentType().equals("application/json")){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (request.getMethod().equals("POST") && (request.getPathInfo()!=null && !request.getPathInfo().equals("/"))){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

        Jsonb jsonb = JsonbBuilder.create();
        IssueBookDTO issue = null;
        try {
            issue = jsonb.fromJson(request.getReader(), IssueBookDTO.class);
            if (!issue.getBookId().matches("B[0-9]{3}")) {
                throw new ValidationException("Invalid Book ID");
            } else if (!issue.getMemberId().matches("M[0-9]{3}")) {
                throw new ValidationException("Invalid Member ID");
            }
        } catch (ValidationException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            e.printStackTrace();
        } catch (JsonbException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid JSON");
            e.printStackTrace();
        }catch (Throwable e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


        try(Connection con = pool.getConnection()){
            PreparedStatement pst = con.prepareStatement("SELECT * FROM Issues WHERE bookId=?");
            pst.setString(1,issue.getBookId());
            ResultSet rset = pst.executeQuery();
            if (rset.next()){
                response.sendError(410,"Book is already issued!");
                return;
            }
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try(Connection con = pool.getConnection()){
            PreparedStatement pst = con.prepareStatement("SELECT * FROM books WHERE id=?");
            pst.setString(1,issue.getBookId());
            ResultSet rset = pst.executeQuery();
            if (!rset.next()){
                response.sendError(410,"Can not find a book!");
                return;
            }
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try(Connection con = pool.getConnection()){
            PreparedStatement pst2 = con.prepareStatement("SELECT * FROM members WHERE id=?");
            pst2.setString(1,issue.getMemberId());
            ResultSet rset = pst2.executeQuery();
            if (!rset.next()){
                response.sendError(410,"Can not find a member!");
                return;
            }
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO Issues(bookId, memberId, dateTime) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, issue.getBookId());
            stm.setString(2, issue.getMemberId());
            stm.setString(3, String.valueOf(LocalDateTime.now()));
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to Issue the book!");
            }
            ResultSet set = stm.getGeneratedKeys();
            set.next();
            issue.setIssueId(set.getString(1));
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Successfully Issue the book!");

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(410);
            response.getWriter().write("Failed to Issue the book!");
        }
    }
}
