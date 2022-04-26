package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.MemberDTO;
import dep8.ijse.lk.exception.ValidationException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;

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
import java.util.ArrayList;

@WebServlet(name = "MemberServlet", urlPatterns ="/members/*")
public class MemberServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    public volatile DataSource pool;

    private void doSaveOrUpdate(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (req.getContentType()== null || !req.getContentType().toLowerCase().startsWith("application/json")){
            res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        if (method.equals("POST") &&
                !((req.getRequestURI().equalsIgnoreCase("/members") ||
                        req.getRequestURI().equalsIgnoreCase("/members/")))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (method.equals("PUT") && !(pathInfo != null &&
                pathInfo.substring(1).matches("M[0-9]{3}"))) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Member does not exist");
            return;
        }

        try {
            Jsonb jsonb = JsonbBuilder.create();
            MemberDTO member = jsonb.fromJson(req.getReader(), MemberDTO.class);
            if (!member.getFirst_name().matches("[A-Za-z ]+") || !member.getLast_name().matches("[A-Za-z ]+")) {
                throw new ValidationException("Invalid Name");
            } else if (!member.getDOB().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new ValidationException("Invalid Date Of Birth");
            } else if (!member.getAddress().matches("[A-Za-z0-9]+")) {
                throw new ValidationException("Invalid Address Type");
            } else if (!member.getNic().matches("^([0-9]{9}[x|X|v|V]|[0-9]{12})$")) {
                throw new ValidationException("Invalid NIC Number");
            }else if (method.equals("POST") && (member.getId()==null || !member.getId().matches("M[0-9]{3}"))){
                throw new ValidationException("Invalid ID");
            }

            if (method.equals("PUT")){
                member.setId(pathInfo.replaceAll("[/]",""));
            }

            try (Connection con = pool.getConnection()) {
                PreparedStatement stm = con.prepareStatement("SELECT * FROM members WHERE id=?");
                stm.setString(1,member.getId());
                ResultSet resultSet = stm.executeQuery();

                if (resultSet.next()) {
                    if (method.equals("POST")) {
                        res.sendError(HttpServletResponse.SC_CONFLICT, "Member already exists!");
                    } else {
                        PreparedStatement stm1 = con.prepareStatement("UPDATE members SET first_name=?, last_name=?, address=?, DOB=?, nic=? WHERE id=?");
                        stm1.setString(1,member.getFirst_name());
                        stm1.setString(2,member.getLast_name());
                        stm1.setString(3,member.getAddress());
                        stm1.setString(4,member.getDOB());
                        stm1.setString(5,member.getNic());
                        stm1.setString(6,member.getId());
                        if (stm1.executeUpdate() != 1) {
                            throw new RuntimeException("Failed to update the member");
                        }
                        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }

                }else {
                    PreparedStatement stm2 = con.prepareStatement("INSERT INTO members(id,first_name,last_name,address,DOB,nic) VALUES (?,?,?,?,?,?)");
                    stm2.setString(1, member.getId());
                    stm2.setString(2, member.getFirst_name());
                    stm2.setString(3, member.getLast_name());
                    stm2.setString(4, member.getAddress());
                    stm2.setString(5, member.getDOB());
                    stm2.setString(6, member.getNic());
                    int i = stm2.executeUpdate();
                    if (i != 1) {
                        throw new RuntimeException("Failed to save the member!");
                    }
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.getWriter().write("Successfully saved the member!");
                }
            }

        } catch (ValidationException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            res.getWriter().write(e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getPathInfo()!=null && !request.getPathInfo().equals("/")){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String query = request.getParameter("q");
        query = "%"  + ((query == null) ? "": query) + "%";

        boolean pagination = request.getParameter("page")!=null && request.getParameter("size")!=null;
        String sql;
        if (pagination){
            sql="SELECT * FROM members WHERE id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR address LIKE ? OR DOB LIKE ? OR nic LIKE ? LIMIT ? OFFSET ?";
        }else {
            sql="SELECT * FROM members WHERE id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR address LIKE ? OR DOB LIKE ? OR nic LIKE ?";
        }

        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement(sql);
            stm.setString(1,query);
            stm.setString(2,query);
            stm.setString(3,query);
            stm.setString(4,query);
            stm.setString(5,query);
            stm.setString(6,query);
            if (pagination){
                int page=Integer.parseInt(request.getParameter("page"));
                int size=Integer.parseInt(request.getParameter("size"));

                stm.setInt(7,size);
                stm.setInt(8,(page -1)*size);
            }

            ResultSet rst = stm.executeQuery();
            Jsonb jsonb = JsonbBuilder.create();
            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();
            while (rst.next()) {
                memberDTOS.add(new MemberDTO(rst.getString("id"), rst.getString("first_name")
                        , rst.getString("last_name"), rst.getString("address"), rst.getString("DOB"),
                        rst.getString("nic")));
            }

            response.setContentType("application/json");
            jsonb.toJson(memberDTOS, response.getWriter());
            memberDTOS.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to fetch data");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doSaveOrUpdate(request,response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doSaveOrUpdate(req,resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getPathInfo()==null || req.getPathInfo().equals("/")){
            resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Unable to delete all the members");
            return;
        }else if (req.getPathInfo() !=null && !req.getPathInfo().substring(1).matches("M[0-9]{3}")){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Member Not Found!");
            return;
        }

        String id = req.getPathInfo().replaceAll("[/]", "");
        try(Connection connection = pool.getConnection()){
            PreparedStatement stm1 = connection.prepareStatement("SELECT * FROM members WHERE id=?");
            stm1.setString(1,id);
            ResultSet rst = stm1.executeQuery();
            if (rst.next()){
                PreparedStatement stm = connection.prepareStatement("DELETE FROM members WHERE id=?");
                stm.setString(1,id);
                if (stm.executeUpdate()!=1){
                    throw new RuntimeException("Failed to Delete the Member!");
                }
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,"Member Not Found!");
            }

        }catch (Throwable e){
            e.printStackTrace();
            resp.getWriter().write("Failed to Delete the Member!");
        }
    }
}
