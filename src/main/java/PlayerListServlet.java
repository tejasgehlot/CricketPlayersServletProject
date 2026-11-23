import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerListServlet extends HttpServlet
{
    private static final String KEY = "PLY_LIST";

    // ---------- Page styling (same theme as index.html) ----------
    private static final String STYLE = """
        :root {
            --turf: #1f5a3a;
            --turf-light: #246640;
            --ink: #10241a;
            --muted: #5a6b60;
            --paper: #eef3ea;
            --card: #ffffff;
            --line: #c9d5c4;
            --ball: #b3261e;
            --radius: 12px;
        }
        * { box-sizing: border-box; }
        [hidden] { display: none !important; }
        html, body { margin: 0; }
        body {
            min-height: 100vh;
            padding: 32px 16px;
            background: var(--paper);
            color: var(--ink);
            font-family: "Bricolage Grotesque", "Segoe UI", system-ui, -apple-system, Roboto, Arial, sans-serif;
            font-size: 16px;
            line-height: 1.5;
        }
        .shell {
            width: 100%;
            max-width: 1040px;
            margin: 0 auto;
            background: var(--card);
            border-radius: 22px;
            overflow: hidden;
            box-shadow: 0 24px 60px -24px rgba(16, 36, 26, 0.35);
        }
        .outfield {
            display: flex;
            align-items: flex-end;
            justify-content: space-between;
            gap: 24px;
            flex-wrap: wrap;
            padding: 40px 44px;
            color: #fff;
            background: repeating-linear-gradient(90deg, var(--turf) 0 64px, var(--turf-light) 64px 128px);
        }
        .outfield h1 {
            margin: 0;
            font-size: clamp(2.2rem, 5vw, 3.6rem);
            font-weight: 800;
            line-height: 1;
            letter-spacing: -0.03em;
        }
        .outfield p { margin: 10px 0 0; color: rgba(255, 255, 255, 0.82); }
        .btn {
            display: inline-block;
            padding: 13px 22px;
            font: inherit;
            font-weight: 700;
            text-decoration: none;
            color: #fff;
            background: var(--ball);
            border: 0;
            border-radius: var(--radius);
            cursor: pointer;
            transition: background 0.15s, transform 0.1s;
        }
        .btn:hover { background: #99201a; }
        .btn:active { transform: translateY(1px); }
        .btn:focus-visible { outline: 3px solid #fff; outline-offset: 3px; }

        .content { padding: 28px 44px 40px; }

        .toolbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 22px;
        }
        .chips { display: flex; gap: 8px; flex-wrap: wrap; }
        .chip {
            padding: 8px 14px;
            font: inherit;
            font-weight: 600;
            font-size: 0.92rem;
            color: var(--ink);
            background: #fff;
            border: 1.5px solid var(--line);
            border-radius: 999px;
            cursor: pointer;
            transition: background 0.15s, border-color 0.15s, color 0.15s;
        }
        .chip span { margin-left: 6px; color: var(--muted); font-weight: 500; }
        .chip:hover { border-color: #9fb199; }
        .chip[aria-pressed="true"] { background: var(--turf); border-color: var(--turf); color: #fff; }
        .chip[aria-pressed="true"] span { color: rgba(255, 255, 255, 0.8); }
        .chip:focus-visible { outline: 3px solid var(--turf); outline-offset: 2px; }

        .search {
            width: 100%;
            max-width: 260px;
            height: 44px;
            padding: 0 14px;
            font: inherit;
            color: var(--ink);
            background: #fff;
            border: 1.5px solid var(--line);
            border-radius: var(--radius);
            transition: border-color 0.15s, box-shadow 0.15s;
        }
        .search:hover { border-color: #9fb199; }
        .search:focus-visible {
            outline: none;
            border-color: var(--turf);
            box-shadow: 0 0 0 4px rgba(31, 90, 58, 0.18);
        }

        .table-wrap { overflow-x: auto; border: 1.5px solid var(--line); border-radius: 16px; }
        table { width: 100%; border-collapse: collapse; min-width: 560px; }
        th {
            padding: 14px 18px;
            text-align: left;
            font-size: 0.9rem;
            font-weight: 600;
            color: var(--muted);
            background: #f5f9f3;
            border-bottom: 1.5px solid var(--line);
        }
        td { padding: 14px 18px; border-bottom: 1px solid #e3eae0; vertical-align: middle; }
        tbody tr:last-child td { border-bottom: 0; }
        tbody tr:hover td { background: #f8fbf6; }
        td.num { width: 56px; color: var(--muted); }

        .player { display: flex; align-items: center; gap: 12px; font-weight: 600; }
        .badge {
            flex: none;
            width: 38px;
            height: 38px;
            border-radius: 50%;
            display: grid;
            place-items: center;
            background: var(--ball);
            color: #fff;
            font-size: 0.85rem;
            font-weight: 700;
        }

        .pill {
            display: inline-block;
            padding: 4px 12px;
            font-size: 0.88rem;
            font-weight: 600;
            border-radius: 999px;
        }
        .pill.batsman    { background: #fbe4e2; color: #8a1d17; }
        .pill.allrounder { background: #e1f0e6; color: #17492f; }
        .pill.bowler     { background: #e2e9f5; color: #23407a; }
        .pill.other      { background: #eceee9; color: var(--ink); }

        .empty, .no-match {
            text-align: center;
            padding: 56px 20px;
            border: 1.5px dashed var(--line);
            border-radius: 16px;
            color: var(--muted);
        }
        .empty h2 { margin: 0 0 6px; color: var(--ink); font-size: 1.4rem; letter-spacing: -0.02em; }
        .empty p { margin: 0 0 22px; }
        .no-match { margin-top: 16px; padding: 28px 20px; }

        @media (max-width: 640px) {
            .outfield { padding: 28px 24px; }
            .content { padding: 24px 20px 32px; }
            .search { max-width: none; }
        }
        @media (prefers-reduced-motion: reduce) { * { transition: none !important; } }
        """;

    // ---------- Search + filter (client side only) ----------
    private static final String SCRIPT = """
        (function () {
            var rows = document.querySelectorAll('tbody tr');
            if (rows.length === 0) return;

            var search = document.getElementById('search');
            var chips = document.querySelectorAll('.chip');
            var noMatch = document.getElementById('noMatch');
            var activeType = 'all';

            function apply() {
                var q = search.value.trim().toLowerCase();
                var shown = 0;
                rows.forEach(function (row) {
                    var okType = activeType === 'all' || row.getAttribute('data-type') === activeType;
                    var okText = q === '' || row.textContent.toLowerCase().indexOf(q) !== -1;
                    row.hidden = !(okType && okText);
                    if (!row.hidden) shown++;
                });
                noMatch.hidden = shown !== 0;
            }

            chips.forEach(function (chip) {
                chip.addEventListener('click', function () {
                    activeType = chip.getAttribute('data-filter');
                    chips.forEach(function (c) { c.setAttribute('aria-pressed', c === chip ? 'true' : 'false'); });
                    apply();
                });
            });

            search.addEventListener('input', apply);
        })();
        """;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        List<Player> playerList = getList();   // safe snapshot copy
        String ctx = req.getContextPath();

        int batsmen = 0, allrounders = 0, bowlers = 0;
        for (Player p : playerList)
        {
            String t = p.getPlayerType();
            if ("Batsman".equalsIgnoreCase(t)) batsmen++;
            else if ("Allrounder".equalsIgnoreCase(t)) allrounders++;
            else if ("Bowler".equalsIgnoreCase(t)) bowlers++;
        }

        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>All players</title>");
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link rel='preconnect' href='https://fonts.gstatic.com' crossorigin>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,400..800&display=swap' rel='stylesheet'>");
        out.println("<style>" + STYLE + "</style>");
        out.println("</head><body>");

        out.println("<main class='shell'>");

        // Header
        out.println("<header class='outfield'>");
        out.println("<div>");
        out.println("<h1>All players</h1>");
        out.println("<p>" + playerList.size() + (playerList.size() == 1 ? " player" : " players") + " in the squad</p>");
        out.println("</div>");
        out.println("<a class='btn' href='" + ctx + "/index.html'>Add player</a>");
        out.println("</header>");

        out.println("<section class='content'>");

        if (playerList.isEmpty())
        {
            out.println("<div class='empty'>");
            out.println("<h2>No players yet</h2>");
            out.println("<p>Add your first player to start building the squad.</p>");
            out.println("<a class='btn' href='" + ctx + "/index.html'>Add player</a>");
            out.println("</div>");
        }
        else
        {
            // Filters + search
            out.println("<div class='toolbar'>");
            out.println("<div class='chips' role='group' aria-label='Filter by player type'>");
            out.println("<button type='button' class='chip' data-filter='all' aria-pressed='true'>All<span>" + playerList.size() + "</span></button>");
            out.println("<button type='button' class='chip' data-filter='Batsman' aria-pressed='false'>Batsman<span>" + batsmen + "</span></button>");
            out.println("<button type='button' class='chip' data-filter='Allrounder' aria-pressed='false'>Allrounder<span>" + allrounders + "</span></button>");
            out.println("<button type='button' class='chip' data-filter='Bowler' aria-pressed='false'>Bowler<span>" + bowlers + "</span></button>");
            out.println("</div>");
            out.println("<input type='search' id='search' class='search' placeholder='Search players' aria-label='Search players'>");
            out.println("</div>");

            // Table
            out.println("<div class='table-wrap'>");
            out.println("<table>");
            out.println("<thead><tr><th>#</th><th>Name</th><th>Age</th><th>State</th><th>Player Type</th></tr></thead>");
            out.println("<tbody>");

            int i = 1;

            for (Player p : playerList)
            {
                out.println("<tr data-type='" + escape(p.getPlayerType()) + "'>");
                out.println("<td class='num'>" + (i++) + "</td>");
                out.println("<td><div class='player'><span class='badge'>" + escape(initials(p.getName())) + "</span><span>" + escape(p.getName()) + "</span></div></td>");
                out.println("<td>" + p.getAge() + "</td>");
                out.println("<td>" + escape(p.getState()) + "</td>");
                out.println("<td><span class='pill " + pillClass(p.getPlayerType()) + "'>" + escape(p.getPlayerType()) + "</span></td>");
                out.println("</tr>");
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("</div>");

            out.println("<div class='no-match' id='noMatch' hidden>No players match your search. Try a different name or filter.</div>");
            out.println("<script>" + SCRIPT + "</script>");
        }

        out.println("</section>");
        out.println("</main>");
        out.println("</body></html>");
    }

    // Returns a copy, taken while holding the list's lock, so iterating it later is safe
    @SuppressWarnings("unchecked")
    private List<Player> getList()
    {
        ServletContext CTX = getServletContext();
        List<Player> playerList = (List<Player>) CTX.getAttribute(KEY);

        if (playerList == null)
        {
            return Collections.emptyList();
        }

        synchronized (playerList)
        {
            return new ArrayList<>(playerList);
        }
    }

    // Initials for the round badge (first letter of first and last word)
    private String initials(String name)
    {
        if (name == null) return "?";

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "?";

        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    // Fixed CSS class from a whitelist, so user text never ends up in a class attribute
    private String pillClass(String playerType)
    {
        if ("Batsman".equalsIgnoreCase(playerType)) return "batsman";
        if ("Allrounder".equalsIgnoreCase(playerType)) return "allrounder";
        if ("Bowler".equalsIgnoreCase(playerType)) return "bowler";
        return "other";
    }

    // Prevents HTML injection (XSS) from user-entered text
    private String escape(String s)
    {
        if (s == null) return "";

        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray())
        {
            switch (c)
            {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#x27;"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
}

//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------
//---------------------------------------------------


//import jakarta.servlet.ServletContext;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class PlayerListServlet extends HttpServlet
//{
//    private static final String KEY = "PLY_LIST";
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
//    {
//        resp.setContentType("text/html");
//        resp.setCharacterEncoding("UTF-8");
//
//        List<Player> playerList = getList();   // safe snapshot copy
//        String ctx = req.getContextPath();
//
//        PrintWriter out = resp.getWriter();
//
//        out.println("<html><body>");
//        out.println("<h2>All Players (" + playerList.size() + ")</h2>");
//        out.println("<a href='" + ctx + "/index.html'>Add Player</a> <br><br>");
//
//        out.println("<table border='1' cellpadding='7' cellspacing='0'>");
//        out.println("<tr><th>#</th><th>Name</th><th>Age</th><th>State</th><th>Player Type</th></tr>");
//
//        int i = 1;
//
//        for (Player p : playerList)
//        {
//            out.println("<tr>");
//            out.println("<td>" + (i++) + "</td>");
//            out.println("<td>" + escape(p.getName()) + "</td>");
//            out.println("<td>" + p.getAge() + "</td>");
//            out.println("<td>" + escape(p.getState()) + "</td>");
//            out.println("<td>" + escape(p.getPlayerType()) + "</td>");
//            out.println("</tr>");
//        }
//
//        out.println("</table>");
//        out.println("</body></html>");
//    }
//
//    // Returns a copy, taken while holding the list's lock, so iterating it later is safe
//    @SuppressWarnings("unchecked")
//    private List<Player> getList()
//    {
//        ServletContext CTX = getServletContext();
//        List<Player> playerList = (List<Player>) CTX.getAttribute(KEY);
//
//        if (playerList == null)
//        {
//            return Collections.emptyList();
//        }
//
//        synchronized (playerList)
//        {
//            return new ArrayList<>(playerList);
//        }
//    }
//
//    // Prevents HTML injection (XSS) from user-entered text
//    private String escape(String s)
//    {
//        if (s == null) return "";
//
//        StringBuilder sb = new StringBuilder(s.length());
//        for (char c : s.toCharArray())
//        {
//            switch (c)
//            {
//                case '&':  sb.append("&amp;");  break;
//                case '<':  sb.append("&lt;");   break;
//                case '>':  sb.append("&gt;");   break;
//                case '"':  sb.append("&quot;"); break;
//                case '\'': sb.append("&#x27;"); break;
//                default:   sb.append(c);
//            }
//        }
//        return sb.toString();
//    }
//}