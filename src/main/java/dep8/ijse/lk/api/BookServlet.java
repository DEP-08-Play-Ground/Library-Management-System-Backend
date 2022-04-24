package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.BookDTO;
import dep8.ijse.lk.dto.MemberDTO;
import dep8.ijse.lk.exception.ValidationException;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.awt.print.Book;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet(name = "BookServlet", urlPatterns = {"/books","/books/"})
public class BookServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    public volatile DataSource pool;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        BookDTO book = null;
        try {
            book = jsonb.fromJson(request.getReader(), BookDTO.class);
            if (!book.getId().matches("B[0-9][0-9][0-9]")) {
                throw new ValidationException("Invalid ID");
            } else if (book.getName()==null) {
                throw new ValidationException("Should add the book name");
            } else if (book.getAuthor()==null) {
                throw new ValidationException("Should add the author");
            } else if (book.getType()==null) {
                throw new ValidationException("Should add the book type");
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
            PreparedStatement stm = connection.prepareStatement("INSERT INTO books(id, name, author, type) VALUES (?,?,?,?)");
            stm.setString(1, book.getId());
            stm.setString(2, book.getName());
            stm.setString(3, book.getAuthor());
            stm.setString(4, book.getType());
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to save the book!");
            }
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Successfully saved the book!");

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().write("Failed to save the book!");
        }
    }
}
