package main.service;

import java.sql.*;

public class DatabaseConnector {
    private static final String URL = "jdbc:sqlite:meeting_room_booking.db";
    private static Connection conn = null;

    public static Connection connect() {
        try {
            if (conn == null || conn.isClosed()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {}
                
                conn = DriverManager.getConnection(URL);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL;");
                }
                
                System.out.println("SQLite Connected (WAL Mode)!");
            }
        } catch (SQLException e) {
            System.out.println("Database connect error: " + e.getMessage());
        }
        return conn;
    }

    public static void initializeDatabase() {
        String sqlBookings = """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                username    TEXT,  
                room_id     INTEGER,
                start_time  TEXT,
                end_time    TEXT,
                purpose     TEXT,
                status      TEXT DEFAULT 'รออนุมัติ'
            );
        """;

        String sqlUsers = """
            CREATE TABLE IF NOT EXISTS users (
                username   TEXT PRIMARY KEY, 
                password   TEXT,
                email      TEXT,
                phone      TEXT,
                role       TEXT DEFAULT 'User',
                status     TEXT DEFAULT 'Active'
            );
        """;

        // [แก้ไข] เพิ่มคอลัมน์สำหรับระบบแจ้งเตือน (Notification System)
        String sqlAlerts = """
            CREATE TABLE IF NOT EXISTS alerts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                alert_date TEXT,
                message TEXT,
                target_user TEXT DEFAULT 'ALL', 
                is_read INTEGER DEFAULT 0,
                read_by TEXT DEFAULT ','
            );
        """;

        String sqlRooms = """
            CREATE TABLE IF NOT EXISTS rooms (
                room_id   INTEGER PRIMARY KEY,
                room_name TEXT
            );
        """;

        String sqlEquipment = """
            CREATE TABLE IF NOT EXISTS equipment (
                equipment_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                equipment_name TEXT NOT NULL,
                quantity       INTEGER DEFAULT 1,
                status         TEXT DEFAULT 'Ready'
            );
        """;

        String sqlBookingEquipment = """
            CREATE TABLE IF NOT EXISTS booking_equipment (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                booking_id      INTEGER,
                equipment_name  TEXT,
                quantity        INTEGER,
                FOREIGN KEY(booking_id) REFERENCES bookings(booking_id)
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlBookings);
            stmt.execute(sqlUsers);
            stmt.execute(sqlAlerts); 
            stmt.execute(sqlRooms);
            stmt.execute(sqlEquipment);
            stmt.execute(sqlBookingEquipment);
            
            // [แก้ไขใหม่] เช็คให้ละเอียดขึ้น เพื่อไม่ให้ขึ้น Error รกหน้าจอ
            try { 
                stmt.execute("ALTER TABLE users ADD COLUMN name TEXT DEFAULT ''"); 
                System.out.println("System: Added 'name' column to users table.");
            } catch(SQLException e) {
                // ถ้า Error ว่ามี column นี้อยู่แล้ว ให้ข้ามไปเงียบๆ ไม่ต้อง printStackTrace
                if (!e.getMessage().contains("duplicate column")) {
                    e.printStackTrace(); 
                }
            }

            // ส่วนของ alerts ก็ทำเหมือนกัน
            try { stmt.execute("ALTER TABLE alerts ADD COLUMN target_user TEXT DEFAULT 'ALL'"); } catch(SQLException e){}
            try { stmt.execute("ALTER TABLE alerts ADD COLUMN is_read INTEGER DEFAULT 0"); } catch(SQLException e){}
            try { stmt.execute("ALTER TABLE alerts ADD COLUMN read_by TEXT DEFAULT ','"); } catch(SQLException e){}
            
            System.out.println("All tables initialized successfully!");
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}