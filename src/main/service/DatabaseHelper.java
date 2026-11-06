package main.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public static Connection getConnection() {
        return DatabaseConnector.connect();
    }

    public static List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC"; 
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }

    public static void addEquipment(String equipName, String status, int quantity) {
        String sql = "INSERT INTO equipment (equipment_name, status, quantity) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipName);
            pstmt.setString(2, status);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            System.out.println("Equipment added successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean deleteEquipmentFromDB(int id) {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}