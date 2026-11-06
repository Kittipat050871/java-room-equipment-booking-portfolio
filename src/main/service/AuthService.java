package main.service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import main.model.*; // Import ทั้งหมดใน model เพื่อเรียกใช้ Info/Warning Notification และ BookingDetail

public class AuthService {

    public static String currentLoggedInStudentId = "";

    // =====================================================
    // HELPER: map room_id -> room_name
    // =====================================================
    public static String mapRoomIdToName(int roomId) {
        return switch (roomId) {
            case 1 -> "ห้องเล็ก01";
            case 2 -> "ห้องเล็ก02";
            case 3 -> "ห้องเล็ก03";
            case 4 -> "ห้องเล็ก04";
            case 5 -> "ห้องเล็ก05";
            case 6 -> "ห้องใหญ่01";
            case 7 -> "ห้องใหญ่02";
            default -> "ไม่มี";
        };
    }

    public int getRoomIdByName(String roomName) {
        if (roomName == null) return 0;
        return switch (roomName) {
            case "ห้องเล็ก01" -> 1;
            case "ห้องเล็ก02" -> 2;
            case "ห้องเล็ก03" -> 3;
            case "ห้องเล็ก04" -> 4;
            case "ห้องเล็ก05" -> 5;
            case "ห้องใหญ่01" -> 6;
            case "ห้องใหญ่02" -> 7;
            default -> 0;
        };
    }

    // =====================================================
    // ตรวจสอบโครงสร้างตารางแจ้งเตือน
    // =====================================================
    private void ensureAlertTable() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {
            
            try { stmt.executeQuery("SELECT target_user FROM alerts LIMIT 1"); } 
            catch (SQLException e) { stmt.executeUpdate("ALTER TABLE alerts ADD COLUMN target_user TEXT DEFAULT 'ALL'"); }

            try { stmt.executeQuery("SELECT is_read FROM alerts LIMIT 1"); } 
            catch (SQLException e) { stmt.executeUpdate("ALTER TABLE alerts ADD COLUMN is_read INTEGER DEFAULT 0"); }

            try { stmt.executeQuery("SELECT read_by FROM alerts LIMIT 1"); } 
            catch (SQLException e) { stmt.executeUpdate("ALTER TABLE alerts ADD COLUMN read_by TEXT DEFAULT ','"); }
            
            // เพิ่ม column type เพื่อรองรับ Polymorphism (ถ้ายังไม่มี)
            try { stmt.executeQuery("SELECT type FROM alerts LIMIT 1"); } 
            catch (SQLException e) { stmt.executeUpdate("ALTER TABLE alerts ADD COLUMN type TEXT DEFAULT 'info'"); }
            
        } catch (Exception e) { }
    }

    // =====================================================
    // 1) LOGIN USER
    // =====================================================
    public boolean login(String inputUser, String password) {
        String sql = "SELECT * FROM users WHERE (username = ? OR name = ?) AND password = ?";
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, inputUser); 
            pstmt.setString(2, inputUser); 
            pstmt.setString(3, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String realUsername = rs.getString("username");
                currentLoggedInStudentId = realUsername; 
                
                updateUserStatus(realUsername, "Active");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    // =====================================================
    // 2) LOGIN ADMIN
    // =====================================================
    public boolean loginAdmin(String user, String pass) {
        // Portfolio safety change: no administrator credentials embedded in source code.
        // Both values must be provided explicitly for a local-only demo session.
        String configuredUser = System.getenv("BOOKING_ADMIN_USER");
        String configuredPassword = System.getenv("BOOKING_ADMIN_PASSWORD");
        if (configuredUser == null || configuredUser.isBlank()
                || configuredPassword == null || configuredPassword.isBlank()
                || user == null || pass == null) {
            return false; // Admin access remains disabled when not configured.
        }
        return configuredUser.equals(user) && configuredPassword.equals(pass);
    }

    // =====================================================
    // 3) REGISTER USER
    // =====================================================
    public boolean register(User newUser) {
        String sql = "INSERT INTO users(username, password, name, email, phone, role, status) VALUES(?,?,?,?,?,?,?)";
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newUser.getStudentId()); 
            pstmt.setString(2, newUser.getPassword());
            pstmt.setString(3, newUser.getName() != null ? newUser.getName() : ""); 
            pstmt.setString(4, newUser.getEmail());
            pstmt.setString(5, newUser.getPhone());
            pstmt.setString(6, "User"); 
            pstmt.setString(7, "Inactive"); 
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Register error: " + e.getMessage());
        }
        return false;
    }

    // =====================================================
    // 4) GET USER DETAILS
    // =====================================================
    public User getUserDetails(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                if (name == null) name = ""; 
                
                user = new User(
                    rs.getString("username"), 
                    "******", 
                    name,       
                    rs.getString("email"), 
                    rs.getString("phone")
                );
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return user;
    }

 // =====================================================
    // 5) GET BOOKING HISTORY (Fixed: Avoid nested connection close)
    // =====================================================
    public List<Booking> getBookingHistory(String username) {
        List<Booking> bookings = new ArrayList<>();
        
        // [แก้ปัญหา] ดึง User มารอไว้ก่อน (นอก Try-Catch ของ Database)
        // เพื่อไม่ให้ Connection ตีกัน
        User currentUserObj = getUserDetails(username); 

        String sql = """
            SELECT b.booking_id, b.username, b.room_id, r.room_name, b.start_time, b.end_time, b.status, b.purpose,
                COALESCE(GROUP_CONCAT(e.equipment_name || ' x' || e.quantity, ', '), 'ไม่มี') AS equipments
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.room_id
            LEFT JOIN booking_equipment e ON b.booking_id = e.booking_id
            WHERE b.username = ? GROUP BY b.booking_id ORDER BY b.booking_id DESC      
            """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String start = rs.getString("start_time");
                    String end   = rs.getString("end_time");
                    String date = start != null && start.contains("T") ? start.split("T")[0] : start;
                    String timeRange = (start != null && start.contains("T") ? start.split("T")[1] : start) + " - " +
                                       (end != null && end.contains("T") ? end.split("T")[1] : end);

                    int roomId = rs.getInt("room_id");
                    String roomName = rs.getString("room_name");
                    if (roomName == null || roomName.isBlank()) roomName = mapRoomIdToName(roomId);

                    Booking b = new Booking(rs.getInt("booking_id"), rs.getString("username"), roomName, date, timeRange,
                            rs.getString("status"), rs.getString("purpose"), rs.getString("equipments"));
                    
                    // [Aggregation] เอา User ที่ดึงไว้ข้างบนมาใส่
                    b.setUser(currentUserObj);

                    bookings.add(b);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return bookings;
    }

    // =====================================================
    // 6) CREATE BOOKING
    // =====================================================
    public boolean createBooking(String username, String roomName, String date, String startTime, String endTime,
                                 String purpose, int equipmentId, int quantity) {
        int roomId = getRoomIdByName(roomName);
        String startISO = date + "T" + startTime;
        String endISO   = date + "T" + endTime;
        String insertBookingSql = "INSERT INTO bookings (username, room_id, start_time, end_time, purpose, status) VALUES (?, ?, ?, ?, ?, ?)";
        String insertBookingEquipSql = "INSERT INTO booking_equipment (booking_id, equipment_name, quantity) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnector.connect();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username); pstmt.setInt(2, roomId); pstmt.setString(3, startISO);
                pstmt.setString(4, endISO); pstmt.setString(5, purpose); pstmt.setString(6, "รออนุมัติ");

                int affected = pstmt.executeUpdate();
                if (affected == 0) { conn.rollback(); return false; }

                try (ResultSet gen = pstmt.getGeneratedKeys()) {
                    if (gen.next()) {
                        int bookingId = gen.getInt(1);
                        if (equipmentId > 0) {
                            String equipName = null;
                            try (PreparedStatement psEq = conn.prepareStatement("SELECT equipment_name FROM equipment WHERE equipment_id = ?")) {
                                psEq.setInt(1, equipmentId);
                                try (ResultSet rsEq = psEq.executeQuery()) { if (rsEq.next()) equipName = rsEq.getString("equipment_name"); }
                            }
                            if (equipName != null) {
                                try (PreparedStatement pstmt2 = conn.prepareStatement(insertBookingEquipSql)) {
                                    pstmt2.setInt(1, bookingId); pstmt2.setString(2, equipName); pstmt2.setInt(3, quantity);
                                    pstmt2.executeUpdate();
                                }
                            }
                        }
                    } else { conn.rollback(); return false; }
                }
            }
            conn.commit(); return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) { }
            return false;
        } finally { try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ex) { } }
    }

    // =====================================================
    // 7) UPDATE STATUS (ADMIN)
    // =====================================================
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("อัปเดตสถานะล้มเหลว: " + e.getMessage());
        }
        return false;
    }

    // =====================================================
    // 8) GET ALL NOTIFICATIONS (Admin View) - Polymorphism
    // =====================================================
    public List<Notification> getAllNotifications() {
        ensureAlertTable();
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts ORDER BY id DESC";
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("alert_date");
                String msg = rs.getString("message");
                String type = rs.getString("type");

                // [Polymorphism]
                if ("warning".equalsIgnoreCase(type) || "reject".equalsIgnoreCase(type)) {
                    list.add(new WarningNotification(id, date, msg));
                } else {
                    list.add(new InfoNotification(id, date, msg));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

 // =====================================================
    // 9) GET PENDING REQUESTS (Fixed: Avoid nested connection close)
    // =====================================================
    public List<Booking> getPendingRequests() {
        List<Booking> list = new ArrayList<>();
        String sql = """
            SELECT b.booking_id, b.username, b.room_id, r.room_name, b.start_time, b.end_time, b.status, b.purpose,
                COALESCE(GROUP_CONCAT(e.equipment_name || ' x' || e.quantity, ', '), 'ไม่มี') AS equipments
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.room_id
            LEFT JOIN booking_equipment e ON b.booking_id = e.booking_id
            WHERE b.status = 'รออนุมัติ' GROUP BY b.booking_id ORDER BY b.start_time ASC
            """;
            
        // Step 1: ดึงข้อมูล Booking ทั้งหมดมาก่อน
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String username = rs.getString("username");
                int roomId = rs.getInt("room_id");
                String roomName = rs.getString("room_name");
                if (roomName == null || roomName.isBlank()) roomName = mapRoomIdToName(roomId);
                
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");
                String date = start != null && start.contains("T") ? start.split("T")[0] : start;
                String timeRange = (start != null && start.contains("T") ? start.split("T")[1] : start) + " - " +
                                   (end != null && end.contains("T") ? end.split("T")[1] : end);

                Booking b = new Booking(bookingId, username, roomName, date, timeRange,
                        rs.getString("status"), rs.getString("purpose"), rs.getString("equipments"));
                
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Step 2: Connection ปิดแล้ว ค่อยมาวนลูปเติม User (Aggregation) ทีหลัง
        for (Booking b : list) {
            User u = getUserDetails(b.getStudentId());
            b.setUser(u);
        }
        
        return list;
    }
    // =====================================================
    // 10) CANCEL BOOKING
    // =====================================================
    public boolean cancelBooking(int bookingId) {
        String sql = "UPDATE bookings SET status = 'ยกเลิกแล้ว' WHERE booking_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // =====================================================
    // 11-12) CHECK ROOM AVAILABLE
    // =====================================================
    public boolean isRoomAvailable(int roomId, String newStart, String newEnd) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND status IN ('รออนุมัติ', 'อนุมัติ', 'อนุมัติแล้ว') AND (start_time < ? AND end_time > ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId); pstmt.setString(2, newEnd); pstmt.setString(3, newStart);
            ResultSet rs = pstmt.executeQuery(); if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false; 
    }
    
    public boolean isRoomAvailable(int roomId, String newStart, String newEnd, int excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND booking_id != ? AND status IN ('รออนุมัติ', 'อนุมัติ', 'อนุมัติแล้ว') AND (start_time < ? AND end_time > ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomId); pstmt.setInt(2, excludeBookingId); pstmt.setString(3, newEnd); pstmt.setString(4, newStart);
            ResultSet rs = pstmt.executeQuery(); if (rs.next()) return rs.getInt(1) == 0; 
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    // =====================================================
    // 13) Get Booking By ID
    // =====================================================
    public Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Booking b = new Booking(
                    rs.getInt("booking_id"),
                    rs.getString("username"),
                    mapRoomIdToName(rs.getInt("room_id")),
                    "", "", rs.getString("status"), rs.getString("purpose"), ""
                );
                // Note: Logic นี้ดึงมาไม่ครบทุก field (เช่นเวลา) แต่พอใช้สำหรับบางเคส
                return b; 
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // =====================================================
    // 14) สร้างแจ้งเตือน (Updated: Save type)
    // =====================================================
    public boolean createAlert(String date, String message, String targetUser, String type) {
        ensureAlertTable(); 
        String sql = "INSERT INTO alerts (alert_date, message, target_user, is_read, read_by, type) VALUES (?, ?, ?, 0, ',', ?)";
        if (targetUser == null || targetUser.isBlank()) targetUser = "ALL";
        if (type == null) type = "info";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setString(2, message);
            pstmt.setString(3, targetUser);
            pstmt.setString(4, type);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
    
    // Overload for backward compatibility (default info)
    public boolean createAlert(String date, String message, String targetUser) {
        return createAlert(date, message, targetUser, "info");
    }

    // =====================================================
    // 16) ดึงรายการแจ้งเตือน User - Polymorphism
    // =====================================================
    public List<Notification> getUserNotifications(String username) {
        ensureAlertTable();
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE target_user = 'ALL' OR target_user = ? ORDER BY id DESC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("alert_date");
                String msg = rs.getString("message");
                String type = rs.getString("type");

                // [Polymorphism Implementation]
                if ("warning".equalsIgnoreCase(type) || "reject".equalsIgnoreCase(type)) {
                    list.add(new WarningNotification(id, date, msg));
                } else {
                    list.add(new InfoNotification(id, date, msg));
                }
            }
            markAsRead(username);
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // =====================================================
    // 17) นับจำนวนข้อความที่ยังไม่ได้อ่าน
    // =====================================================
    public int getUnreadCount(String username) {
        ensureAlertTable(); 
        String sql = "SELECT COUNT(*) FROM alerts WHERE " +
                     "(target_user = ? AND is_read = 0) " +
                     "OR " +
                     "(target_user = 'ALL' AND (read_by IS NULL OR read_by NOT LIKE ?))";
                      
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, "%," + username + ",%"); 
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // =====================================================
    // 18) Mark as Read
    // =====================================================
    public void markAsRead(String username) {
        String sqlPrivate = "UPDATE alerts SET is_read = 1 WHERE target_user = ?";
        String sqlAll = "UPDATE alerts SET read_by = IFNULL(read_by, ',') || ? || ',' WHERE target_user = 'ALL' AND (read_by IS NULL OR read_by NOT LIKE ?)";
        
        try (Connection conn = DatabaseConnector.connect()) {
             try (PreparedStatement p1 = conn.prepareStatement(sqlPrivate)) {
                 p1.setString(1, username);
                 p1.executeUpdate();
             }
             try (PreparedStatement p2 = conn.prepareStatement(sqlAll)) {
                 p2.setString(1, username);
                 p2.setString(2, "%," + username + ",%");
                 p2.executeUpdate();
             }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =====================================================
    // Update User Status
    // =====================================================
    public void updateUserStatus(String username, String newStatus) {
        String sql = "UPDATE users SET status = ? WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public boolean updateAccountDetails(String studentId, String email, String phone) {
        String sql = "UPDATE users SET email = ?, phone = ? WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email); pstmt.setString(2, phone); pstmt.setString(3, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    
    public boolean resetPassword(String identifier, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ? OR email = ? OR phone = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword); pstmt.setString(2, identifier); pstmt.setString(3, identifier); pstmt.setString(4, identifier);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public void cancelAllPendingBookings(String studentId) {
        String sql = "UPDATE bookings SET status = 'ยกเลิกแล้ว' WHERE username = ? AND status IN ('รออนุมัติ', 'อนุมัติแล้ว')";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public boolean isUserExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
            
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
    
    // =====================================================
    // Send Booking Alert (Updated: Set type)
    // =====================================================
    public void sendBookingAlert(int bookingId, String status) {
        String sqlInfo = "SELECT username, room_id, start_time FROM bookings WHERE booking_id = ?";
        String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH).format(new java.util.Date());

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlInfo)) {
            
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String targetUser = rs.getString("username");
                int roomId = rs.getInt("room_id");
                String roomName = mapRoomIdToName(roomId);
                
                String message = "";
                String type = "info"; // default

                if (status.contains("อนุมัติแล้ว")) { // ต้องตรงกับค่าใน DB
                    message = "รายการจอง " + roomName + " ของคุณได้รับการ 'อนุมัติ' แล้ว";
                    type = "info";
                } else if (status.contains("ไม่") || status.contains("ยกเลิก") || status.contains("Reject")) {
                    message = "รายการจอง " + roomName + " ของคุณถูก 'ปฏิเสธ/ยกเลิก'";
                    type = "warning";
                } else {
                    message = "รายการจอง " + roomName + " ของคุณสถานะเป็น: " + status;
                    type = "info";
                }
                
                createAlert(dateStr, message, targetUser, type);
                System.out.println("Auto-alert sent to " + targetUser + " (" + type + ")");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    // =====================================================
    // ลบผู้ใช้ถาวร
    // =====================================================
    public boolean deleteUserPermanently(String username) {
        Connection conn = null;
        try {
            conn = DatabaseConnector.connect();
            conn.setAutoCommit(false); 

            // 1. ลบอุปกรณ์
            try (PreparedStatement p1 = conn.prepareStatement(
                    "DELETE FROM booking_equipment WHERE booking_id IN (SELECT booking_id FROM bookings WHERE username = ?)")) {
                p1.setString(1, username);
                p1.executeUpdate();
            }

            // 2. ลบประวัติ
            try (PreparedStatement p2 = conn.prepareStatement("DELETE FROM bookings WHERE username = ?")) {
                p2.setString(1, username);
                p2.executeUpdate();
            }

            // 3. ลบแจ้งเตือน
            try (PreparedStatement p3 = conn.prepareStatement("DELETE FROM alerts WHERE target_user = ?")) {
                p3.setString(1, username);
                p3.executeUpdate();
            }
            
            // 4. ลบ User
            try (PreparedStatement p4 = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                p4.setString(1, username);
                int affected = p4.executeUpdate();
                
                if (affected > 0) {
                    conn.commit(); 
                    System.out.println("User " + username + " deleted permanently.");
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }
    
    public List<String> getAllEquipmentNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT equipment_name FROM equipment"; 
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("equipment_name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public String importUsersFromCSV(java.io.File file) {
        int success = 0;
        int fail = 0;
        StringBuilder log = new StringBuilder();

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; 
                line = line.replace("\uFEFF", "");

                String[] data = line.split(",");
                
                if (data.length >= 5) {
                	String id = data[0].trim();
                	String name = data[1].trim(); 
                	String pass = data[2].trim(); 
                	String email = data[3].trim();
                	String phone = data[4].trim();

                    User newUser = new User(id, pass, name, email, phone);
                    if (register(newUser)) {
                        success++;
                    } else {
                        fail++;
                        log.append("- ").append(id).append(" (ซ้ำ/ผิดพลาด)\n");
                    }
                } else {
                    fail++;
                    log.append("- รูปแบบผิด: ").append(line).append("\n");
                }
            }
        } catch (Exception e) {
            return "เกิดข้อผิดพลาดในการอ่านไฟล์: " + e.getMessage();
        }

        return String.format("นำเข้าสำเร็จ: %d\nไม่สำเร็จ: %d\nรายละเอียด:\n%s", 
                             success, fail, log.toString());
    }
}