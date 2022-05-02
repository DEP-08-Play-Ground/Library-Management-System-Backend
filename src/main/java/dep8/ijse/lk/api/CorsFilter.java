package dep8.ijse.lk.api;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "CorsFilter")
public class CorsFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String origin = req.getHeader("Origin");
        if (origin!=null && origin.contains(getServletContext().getInitParameter("origin"))){
            res.setHeader("Access-Control-Allow-Origin",origin);
            if (req.getMethod().equals("OPTION")){
                res.setHeader("Access-Control-Allow-Methods","GET, POST, PUT, DELETE, OPTION, HEADER");
                res.setHeader("Access-Control-Allow-Headers","Content-Type");
                res.setHeader("Access-Control-Expose-Headers","Content-Type");
                res.setHeader("Access-Control-Expose-Headers","X-Count");
            }
        }
        chain.doFilter(req,res);
    }
}
