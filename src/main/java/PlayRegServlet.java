import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@WebServlet("/reg")
public class PlayRegServlet extends HttpServlet
{
    private static final String KEY = "PLY_LIST";

    // Someone opened /reg directly in the browser (GET) -> send them to the form
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        res.sendRedirect(req.getContextPath() + "/index.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");

        String name = trim(req.getParameter("name"));
        String ageStr = trim(req.getParameter("age"));
        String state = trim(req.getParameter("state"));
        String playerType = trim(req.getParameter("playerType"));

        if (name.isEmpty() || ageStr.isEmpty() || state.isEmpty() || playerType.isEmpty())
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
            return;
        }

        int age;
        try
        {
            age = Integer.parseInt(ageStr);
        }
        catch (NumberFormatException e)
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Age must be a valid number.");
            return;
        }

        if (age < 1 || age > 120)
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Age must be between 1 and 120.");
            return;
        }

        Player player = new Player(name, age, state, playerType);

        List<Player> playerList = getOrCreatList();
        playerList.add(player);

        res.sendRedirect(req.getContextPath() + "/players");
    }

    private String trim(String value)
    {
        return value == null ? "" : value.trim();
    }

    @SuppressWarnings("unchecked")
    private List<Player> getOrCreatList()
    {
        ServletContext CTX = getServletContext();
        synchronized (CTX)
        {
            List<Player> playerList = (List<Player>) CTX.getAttribute(KEY);
            if (playerList == null)
            {
                playerList = Collections.synchronizedList(new ArrayList<>());
                CTX.setAttribute(KEY, playerList);
            }
            return playerList;
        }
    }
}