//package dep8.ijse.lk.api;
//
//import dep8.ijse.lk.dto.BookDTO;
//import dep8.ijse.lk.exception.ValidationException;
//import jakarta.json.bind.Jsonb;
//import jakarta.json.bind.JsonbBuilder;
//
//import javax.annotation.Resource;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.annotation.*;
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//
//@WebServlet(name = "BookServlet", urlPatterns = {"/v1/books/*"})
//public class BookServlet extends HttpServlet {
//
//    @Resource(name = "java:comp/env/jdbc/pool4lms")
//    public volatile DataSource pool;
//
//    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
//        if (req.getContentType()== null || !req.getContentType().toLowerCase().startsWith("application/json")){
//            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
//            return;
//        }
//
//        String method = req.getMethod();
//        String pathInfo = req.getPathInfo();
//
//        if (method.equals("POST") &&
//                !((req.getRequestURI().equalsIgnoreCase("/books") ||
//                        req.getRequestURI().equalsIgnoreCase("/books/")))) {
//            res.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        } else if (method.equals("PUT") && !(pathInfo != null &&
//                pathInfo.substring(1).matches("B[0-9]{3}"))) {
//            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Book does not exist");
//            return;
//        }
//
//        try {
//            Jsonb jsonb = JsonbBuilder.create();
//            BookDTO book = jsonb.fromJson(req.getReader(), BookDTO.class);
//            if (method.equals("POST") && !book.getId().matches("B[0-9]{3}")) {
//                throw new ValidationException("Invalid ID");
//            } else if (book.getName()==null) {
//                throw new ValidationException("Should add the book name");
//            } else if (book.getAuthor()==null) {
//                throw new ValidationException("Should add the author");
//            } else if (book.getType()==null) {
//                throw new ValidationException("Should add the book type");
//            }
//
//            if (method.equals("PUT")){
//                book.setId(pathInfo.replaceAll("[/]",""));
//            }
//
//            try (Connection con = pool.getConnection()) {
//                PreparedStatement stm = con.prepareStatement("SELECT * FROM books WHERE id=?");
//                stm.setString(1,book.getId());
//                ResultSet resultSet = stm.executeQuery();
//
//                if (resultSet.next()) {
//                    if (method.equals("POST")) {
//                        res.sendError(HttpServletResponse.SC_CONFLICT, "Book already exists!");
//                    } else {
//                        PreparedStatement stm1 = con.prepareStatement("UPDATE books SET name=?, author=?, booktype=? WHERE id=?");
//                        stm1.setString(1,book.getName());
//                        stm1.setString(2,book.getAuthor());
//                        stm1.setString(3,book.getType());
//                        stm1.setString(4,book.getId());
//                        if (stm1.executeUpdate()!=1){
//                            throw new RuntimeException("Failed to Update the Book!");
//                        }
//                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
//                    }
//
//                }else {
//                    PreparedStatement stm2 = con.prepareStatement("INSERT INTO books(id, name, author, booktype) VALUES (?,?,?,?)");
//                    stm2.setString(1, book.getId());
//                    stm2.setString(2, book.getName());
//                    stm2.setString(3, book.getAuthor());
//                    stm2.setString(4, book.getType());
//                    int i = stm2.executeUpdate();
//                    if (i != 1) {
//                        throw new RuntimeException("Failed to save the book!");
//                    }
//                    res.setStatus(HttpServletResponse.SC_CREATED);
//                    res.getWriter().write("Successfully saved the Book!");
//                }
//            }
//
//        } catch (ValidationException e) {
//            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
//            res.getWriter().write(e.getMessage());
//            e.printStackTrace();
//        } catch (Throwable e) {
//            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        if (request.getPathInfo()!=null && !request.getPathInfo().equals("/")){
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        String query = request.getParameter("q");
//        query = "%"  + ((query == null) ? "": query) + "%";
//        boolean pagination = request.getParameter("page")!=null && request.getParameter("size")!=null;
//
//        String sql;
//        if (pagination){
//            sql="SELECT * FROM books WHERE id LIKE ? OR name LIKE ? OR author LIKE ? OR booktype LIKE ? LIMIT ? OFFSET ?";
//        }else {
//            sql="SELECT * FROM books WHERE id LIKE ? OR name LIKE ? OR author LIKE ? OR booktype LIKE ?";
//        }
//
//        try (Connection connection = pool.getConnection()) {
//            PreparedStatement stm = connection.prepareStatement(sql);
//            stm.setString(1,query);
//            stm.setString(2,query);
//            stm.setString(3,query);
//            stm.setString(4,query);
//            if (pagination){
//                int page=Integer.parseInt(request.getParameter("page"));
//                int size=Integer.parseInt(request.getParameter("size"));
//                stm.setInt(5,size);
//                stm.setInt(6,(page -1)*size);
//            }
//            ResultSet rst = stm.executeQuery();
//            Jsonb jsonb = JsonbBuilder.create();
//            ArrayList<BookDTO> bookDTOS = new ArrayList<>();
//            while (rst.next()) {
//                bookDTOS.add(new BookDTO(rst.getString("id"), rst.getString("name")
//                        , rst.getString("author"), rst.getString("type")));
//            }
//
//            response.setContentType("application/json");
//            jsonb.toJson(bookDTOS, response.getWriter());
//            bookDTOS.clear();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("Failed to fetch data");
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        doSaveOrUpdate(request,response);
//    }
//
//    @Override
//    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        doSaveOrUpdate(req,resp);
//    }
//
//    @Override
//    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        if (req.getPathInfo()==null || req.getPathInfo().equals("/")){
//            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Unable to delete all the books");
//            return;
//        }else if (req.getPathInfo() !=null && !req.getPathInfo().substring(1).matches("B[0-9]{3}")){
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Book Not Found!");
//            return;
//        }
//
//        String id = req.getPathInfo().replaceAll("[/]", "");
//        try(Connection connection = pool.getConnection()){
//            PreparedStatement stm1 = connection.prepareStatement("SELECT * FROM books WHERE id=?");
//            stm1.setString(1,id);
//            ResultSet rst = stm1.executeQuery();
//            if (rst.next()){
//                PreparedStatement stm = connection.prepareStatement("DELETE FROM books WHERE id=?");
//                stm.setString(1,id);
//                if (stm.executeUpdate()!=1){
//                    throw new RuntimeException("Failed to Delete the book!");
//                }
//                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
//            }else {
//                resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Book Not Found!");
//            }
//
//        }catch (Throwable e){
//            e.printStackTrace();
//            resp.getWriter().write("Failed to Delete the book!");
//        }
//    }
//}
