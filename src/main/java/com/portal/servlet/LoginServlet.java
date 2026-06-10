package com.portal.servlet;

import com.portal.util.DatabaseConnection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    public void doPost(HttpServletRequest req, HttpServletResponse res){
        String userName = req.getParameter("username");
        String password = req.getParameter("password");
        DatabaseConnection databaseConnection = new DatabaseConnection();
        try {
            if(databaseConnection.validateUser(userName,password)){
                HttpSession session = req.getSession();
                session.setAttribute("username",userName);
                res.sendRedirect("dashboard");
            }
            else{
                req.setAttribute("message","Invalid credentials");
                RequestDispatcher rq = req.getRequestDispatcher("index.html");
                rq.forward(req,res);
            }
        } catch (SQLException | ClassNotFoundException | IOException | ServletException e) {
            throw new RuntimeException(e);
        }

    }
}