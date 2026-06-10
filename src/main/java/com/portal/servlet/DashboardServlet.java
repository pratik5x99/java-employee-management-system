package com.portal.servlet;

import com.portal.util.DatabaseConnection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("username") == null) {
            res.sendRedirect("index.html");
            return;
        }

        String userName = (String) session.getAttribute("username");

        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        out.println("<h2>Hi " + userName + "!</h2>");

        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            ArrayList<String> employees = databaseConnection.getAllUsers();

            out.println("<h3>List of all employees:</h3>");
            out.println("<ul>");

            for (String e : employees) {
                out.println("<li>" + e + "</li>");
            }

            out.println("</ul>");
            out.println("<br><a href='logout'>Logout</a>");

        } catch (Exception e) {
            out.println("<p>Error loading database: " + e.getMessage() + "</p>");
        }
    }
}