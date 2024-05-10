package mg.p16.Spring;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.IOException;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
// javac -cp "C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\Spring\WEB-INF\lib\*" FrontServlet.java
public class FrontServlet extends HttpServlet {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                String url = request.getRequestURL().toString();
                PrintWriter out = response.getWriter();
                response.setContentType("text/html");
                out.println(url);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "FrontServlet";
    }
}
