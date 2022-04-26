package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.BookDT02;
import dep8.ijse.lk.exception.ValidationException;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@MultipartConfig(location = "/tmp", maxFileSize = 15*1024*1024)
@WebServlet(name = "BookServlet2", urlPatterns ="/v2/books/*")
public class BookServlet2 extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    private volatile DataSource pool;
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveOrUpdate(req,resp);
    }

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if (req.getContentType()== null || !req.getContentType().toLowerCase().startsWith("multipart/form-data")){
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        if (method.equals("POST") && (pathInfo!=null && !pathInfo.equals("/"))){
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (method.equals("PUT") && !(pathInfo != null &&
                pathInfo.substring(1).matches("B[0-9]{3}"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Book does not exist");
            return;
        }

        try {
            String id =req.getParameter("id");
            String name =req.getParameter("name");
            String author =req.getParameter("author");
            String booktype =req.getParameter("booktype");
            Part preview =req.getPart("preview");

            BookDT02 book;
            if (preview!=null && !preview.getSubmittedFileName().isEmpty()){
                if (!preview.getContentType().toLowerCase().startsWith("image/")){
                    throw new ValidationException("Invalid image type");
                }

                InputStream is = preview.getInputStream();
                byte[] buffer = new byte[(int) preview.getSize()];
                is.read(buffer);
                book = new BookDT02(id, name, author, booktype, buffer);

            }else {
                book = new BookDT02(id, name, author, booktype);
            }

            if (method.equals("POST") && !book.getId().matches("B[0-9]{3}")) {
                throw new ValidationException("Invalid ID");
            } else if (book.getName()==null) {
                throw new ValidationException("Should add the book name");
            } else if (book.getAuthor()==null) {
                throw new ValidationException("Should add the author");
            } else if (book.getType()==null) {
                throw new ValidationException("Should add the book type");
            }

            if (method.equals("PUT")){
                book.setId(pathInfo.replaceAll("[/]",""));
            }

            try (Connection con = pool.getConnection()) {
                PreparedStatement stm = con.prepareStatement("SELECT * FROM books WHERE id=?");
                stm.setString(1,book.getId());
                ResultSet resultSet = stm.executeQuery();

                if (resultSet.next()) {
                    if (method.equals("POST")) {
                        res.sendError(HttpServletResponse.SC_CONFLICT, "Book already exists!");
                    } else {
                        PreparedStatement stm1 = con.prepareStatement("UPDATE books SET name=?, author=?, booktype=?, preview=? WHERE id=?");
                        stm1.setString(1,book.getName());
                        stm1.setString(2,book.getAuthor());
                        stm1.setString(3,book.getType());
                        stm1.setBlob(4,book.getPreview()==null? null:new SerialBlob(book.getPreview()));
                        stm1.setString(5,book.getId());
                        if (stm1.executeUpdate()!=1){
                            throw new RuntimeException("Failed to Update the Book!");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }

                }else {
                    PreparedStatement stm2 = con.prepareStatement("INSERT INTO books(id, name, author, booktype, preview) VALUES (?,?,?,?,?)");
                    stm2.setString(1, book.getId());
                    stm2.setString(2, book.getName());
                    stm2.setString(3, book.getAuthor());
                    stm2.setString(4, book.getType());
                    stm2.setBlob(5, book.getPreview()==null? null:new SerialBlob(book.getPreview()));
                    int i = stm2.executeUpdate();
                    if (i != 1) {
                        throw new RuntimeException("Failed to save the book!");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.getWriter().write("Successfully saved the Book!");
                }
            }

        } catch (ValidationException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,e.getMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo()==null || req.getPathInfo().equals("/")){
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Unable to delete all the books");
            return;
        }else if (req.getPathInfo() !=null && !req.getPathInfo().substring(1).matches("B[0-9]{3}")){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Book Not Found!");
            return;
        }

        String id = req.getPathInfo().replaceAll("[/]", "");
        try(Connection connection = pool.getConnection()){
            PreparedStatement stm1 = connection.prepareStatement("SELECT * FROM books WHERE id=?");
            stm1.setString(1,id);
            ResultSet rst = stm1.executeQuery();
            if (rst.next()){
                PreparedStatement stm = connection.prepareStatement("DELETE FROM books WHERE id=?");
                stm.setString(1,id);
                if (stm.executeUpdate()!=1){
                    throw new RuntimeException("Failed to Delete the book!");
                }
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Book Not Found!");
            }

        }catch (Throwable e){
            e.printStackTrace();
            resp.getWriter().write("Failed to Delete the book!");
        }

    }
}
