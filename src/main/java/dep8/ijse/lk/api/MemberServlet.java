package dep8.ijse.lk.api;

import dep8.ijse.lk.dto.MemberDTO;
import dep8.ijse.lk.exception.ValidationException;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
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
import java.util.Date;
import java.util.List;


@WebServlet(name = "MemberServlet", urlPatterns = {"/mems", "/mems/"})
public class MemberServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool4lms")
    public volatile DataSource pool;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM members");
            ResultSet rst = stm.executeQuery();
            Jsonb jsonb = JsonbBuilder.create();
            int x = 0;
            ArrayList<MemberDTO> memberDTOS = new ArrayList<>();
            while (rst.next()) {
                memberDTOS.add(x, new MemberDTO(rst.getString("id"), rst.getString("first_name")
                        , rst.getString("last_name"), rst.getString("address"), rst.getString("DOB"),
                        rst.getString("nic")));
                x++;
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
        Jsonb jsonb = JsonbBuilder.create();
        MemberDTO member = null;
        try {
            member = jsonb.fromJson(request.getReader(), MemberDTO.class);
            if (!member.getFirst_name().matches("[A-Za-z ]+") || !member.getLast_name().matches("[A-Za-z ]+")) {
                throw new ValidationException("Invalid Name");
            } else if (!member.getDOB().matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new ValidationException("Invalid Date Of Birth");
            } else if (!member.getAddress().matches("[A-Za-z0-9]+")) {
                throw new ValidationException("Invalid Address Type");
            } else if (!member.getNic().matches("^([0-9]{9}[x|X|v|V]|[0-9]{12})$")) {
                throw new ValidationException("Invalid NIC Number");
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
            PreparedStatement stm = connection.prepareStatement("INSERT INTO members(id,first_name,last_name,address,DOB,nic) VALUES (?,?,?,?,?,?)");
            stm.setString(1, member.getId());
            stm.setString(2, member.getFirst_name());
            stm.setString(3, member.getLast_name());
            stm.setString(4, member.getAddress());
            stm.setString(5, member.getDOB());
            stm.setString(6, member.getNic());
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to save the member!");
            }
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("Successfully saved the member!");

        } catch (Throwable e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.getWriter().write("Failed to save the member!");
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        MemberDTO dto = jsonb.fromJson(req.getReader(), MemberDTO.class);
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("UPDATE members SET first_name=?, last_name=?, address=?, DOB=?, nic=? WHERE id=?");
            stm.setString(1,dto.getFirst_name());
            stm.setString(2,dto.getLast_name());
            stm.setString(3,dto.getAddress());
            stm.setString(4,dto.getDOB());
            stm.setString(5,dto.getNic());
            stm.setString(6,dto.getId());
            if (stm.executeUpdate()!=1){
                throw new RuntimeException("Failed to Update the Member!");
            }
            resp.setStatus(HttpServletResponse.SC_GONE);
            resp.getWriter().write("Successfully Updated!");
        } catch (JsonbException e) {
            e.printStackTrace();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update the Member!");
        }catch (Throwable e){
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            resp.getWriter().write(e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        String header = resp.getHeader("Content-Type");

    }
}
