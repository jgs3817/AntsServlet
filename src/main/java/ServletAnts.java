import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(urlPatterns={"/mainpage"},loadOnStartup = 1)
public class ServletAnts extends HttpServlet {

    public ServletAnts(){}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        resp.setContentType("text/html");
        resp.getWriter().write("Testing servlet");
        System.out.println(req.getServletPath());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        //System.out.println(reqBody);
        resp.setContentType("text/html");
        resp.getWriter().write("Thank you client!");

        Gson gson = new Gson();
        SubmitData submitData = gson.fromJson(reqBody, SubmitData.class);
        System.out.println("Ant data: " + submitData.antDataReturn());
    }
}
