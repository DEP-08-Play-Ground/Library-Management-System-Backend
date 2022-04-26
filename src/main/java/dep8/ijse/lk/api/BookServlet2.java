package dep8.ijse.lk.api;

import com.sun.deploy.net.HttpResponse;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@MultipartConfig(location = "/tmp", maxFileSize = 15*1024*1024)
@WebServlet(name = "BookServlet2", urlPatterns ="/v2/books/*")
public class BookServlet2 extends HttpServlet {

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

////        System.out.println(req.getPart("id").getContentType());
//        System.out.println(req.getParameter("id"));
////        System.out.println(req.getPart("name").getContentType());
//        System.out.println(req.getParameter("name"));
////        System.out.println(req.getPart("author").getContentType());
//        System.out.println(req.getParameter("author"));
////        System.out.println(req.getPart("booktype").getContentType());
//        System.out.println(req.getParameter("booktype"));
//
////        System.out.println(req.getPart("preview").getContentType());


    }
}
