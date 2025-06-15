// logic/LoginChecker.java
package logic;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginChecker {

    public static String[] loginWithRole(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT role, user_id FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new String[]{rs.getString("role"), String.valueOf(rs.getInt("user_id"))};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // failed login
    }
}
