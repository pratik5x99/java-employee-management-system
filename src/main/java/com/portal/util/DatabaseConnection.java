package com.portal.util;

import java.sql.*;

public class DatabaseConnection {
    private String url = "jdbc:mysql://localhost:3306/jdbc_practice";
    private String uname = "root";
    private String password = "Pratik@1817";
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url,uname,password);
    }

    public boolean validateUser(String username,String pass) throws SQLException, ClassNotFoundException {
        String querry = "SELECT * FROM emp WHERE username = ? AND pass = ?";
        try(Connection con = getConnection(); PreparedStatement st = con.prepareStatement(querry)){
            st.setString(1,username);
            st.setString(2,pass);
            ResultSet resultSet = st.executeQuery();
            return resultSet.next();
        }
        catch (Exception e){
            System.out.println(e);
        }
        return false;
    }
}