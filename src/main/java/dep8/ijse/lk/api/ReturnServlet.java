//package dep8.ijse.lk.api;
//
//import dep8.ijse.lk.dto.IssueBookDTO;
//import dep8.ijse.lk.dto.returnDTO;
//import dep8.ijse.lk.exception.ValidationException;
//import jakarta.json.bind.Jsonb;
//import jakarta.json.bind.JsonbBuilder;
//import jakarta.json.bind.JsonbException;
//
//import javax.annotation.Resource;
//import javax.servlet.*;
//import javax.servlet.http.*;
//import javax.servlet.annotation.*;
//import javax.sql.DataSource;
//import java.io.IOException;
//import java.sql.*;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//
//@WebServlet(name = "ReturnServlet", value = "return/*")
//public class ReturnServlet extends HttpServlet {
//    @Resource(name = "java:comp/env/jdbc/pool4lms")
//    public volatile DataSource pool;
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        if(request.getPathInfo()!=null && !request.getPathInfo().equals("/")){
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//        String query = request.getParameter("q");
//        query = "%"  + ((query == null) ? "": query) + "%";
//
//        boolean paginations = request.getParameter("page")!=null && request.getParameter("size")!=null;
//        String sql=null;
//        if (paginations){
//            sql="SELECT * FROM `return` WHERE bookId LIKE ? OR memberId LIKE ? LIMIT ? OFFSET ?";
//        }else {
//            sql="SELECT * FROM `return` WHERE bookId LIKE ? OR memberId LIKE ?";
//        }
//        try (Connection connection = pool.getConnection()) {
//            PreparedStatement stm = connection.prepareStatement(sql);
//            stm.setString(1,query);
//            stm.setString(2,query);
//            if (paginations){
//                int page = Integer.parseInt(request.getParameter("page"));
//                int size = Integer.parseInt(request.getParameter("size"));
//                stm.setInt(3,page);
//                stm.setInt(4,(page-1)*size);
//            }
//            ResultSet rst = stm.executeQuery();
//            PreparedStatement pst = connection.prepareStatement("SELECT count(*) FROM `return` WHERE bookId LIKE ? OR memberId LIKE ?");
//            pst.setString(1,query);
//            pst.setString(2,query);
//            ResultSet rest = pst.executeQuery();
//            if (rest.next()){
//                response.setHeader("X-Count",rest.getString(1));
//            }else {
//                response.setHeader("X-Count","0");
//            }
//
//            Jsonb jsonb = JsonbBuilder.create();
//            ArrayList<returnDTO> issues = new ArrayList<>();
//            while (rst.next()) {
//                issues.add(new returnDTO(rst.getString("returnId"), rst.getString("bookId")
//                        , rst.getString("memberId"), rst.getString("datetime")));
//            }
//            response.setContentType("application/json");
//            jsonb.toJson(issues, response.getWriter());
//            issues.clear();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("Failed to fetch data");
//        }
//
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        if (request.getContentType()==null || !request.getContentType().equals("application/json")){
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//        if (request.getPathInfo()==null || !(request.getPathInfo()!=null && request.getPathInfo().replaceAll("/","").matches("B\\d{3}]"))){
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
//            return;
//        }
//
//        String bookId = request.getPathInfo().replaceAll("/", "");
//        Connection con=null;
//        try{
//            con = pool.getConnection();
//            PreparedStatement pst = con.prepareStatement("SELECT * FROM Issues WHERE bookId=?");
//            pst.setString(1,bookId);
//            ResultSet rset = pst.executeQuery();
//            if (!rset.next()){
//                response.sendError(HttpServletResponse.SC_NOT_FOUND,"Book not found!");
//                return;
//            }
//            con.setAutoCommit(true);
//            PreparedStatement stm = con.prepareStatement("INSERT INTO `return`(bookId, memberId, dateTime) VALUES (?,?,?)");
//            stm.setString(1, bookId);
//            stm.setString(2, rset.getString("bookId"));
//            stm.setString(3, String.valueOf(LocalDateTime.now()));
//            int i = stm.executeUpdate();
//            if (i != 1) {
//                throw new RuntimeException("Failed to return the book!");
//            }
//            PreparedStatement statement = con.prepareStatement("DELETE FROM Issues WHERE bookId=?");
//            int i1 = statement.executeUpdate();
//            if (i1!=1){
//                throw new RuntimeException("Failed to return the book!");
//            }
//            con.commit();
//            response.setStatus(HttpServletResponse.SC_GONE);
//            response.getWriter().write("Successfully return the book!");
//        }catch (Throwable e){
//            try {
//                con.rollback();
//            } catch (SQLException ex) {
//                throw new RuntimeException(ex);
//            }
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }finally {
//            try {
//                con.setAutoCommit(true);
//                con.close();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//    }
//}
