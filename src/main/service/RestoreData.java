package main.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class RestoreData {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:meeting_room_booking.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            System.out.println("🔄 กำลังซ่อมแซมข้อมูล...");

            // 1. แก้ชื่ออุปกรณ์ให้ตรงกับ Logic ของหน้า UI (C=กล้อง, PP=Pro, PG=Gen)
            stmt.execute("UPDATE equipment SET equipment_name = 'C-Camera Set 1' WHERE equipment_id = 1");
            stmt.execute("UPDATE equipment SET equipment_name = 'C-Tripod Stand' WHERE equipment_id = 2");
            stmt.execute("UPDATE equipment SET equipment_name = 'PP-iPad Pro (M2)' WHERE equipment_id = 3");
            stmt.execute("UPDATE equipment SET equipment_name = 'PG-iPad Gen 10' WHERE equipment_id = 4");

            // 2. เพิ่มอุปกรณ์ให้ดูเยอะขึ้น (จะได้ไม่โล่ง)
            String[] moreEquip = {
                "INSERT INTO equipment (equipment_name, status) VALUES ('C-Camera Set 2', 'Ready')",
                "INSERT INTO equipment (equipment_name, status) VALUES ('C-Camera Set 3', 'Broken')",
                "INSERT INTO equipment (equipment_name, status) VALUES ('PP-iPad Pro (M1)', 'Ready')",
                "INSERT INTO equipment (equipment_name, status) VALUES ('PG-iPad Gen 9', 'Ready')"
            };
            for (String sql : moreEquip) {
                try { stmt.execute(sql); } catch (Exception e) {}
            }

            System.out.println("✅ กู้คืนข้อมูลเสร็จแล้ว! หน้า 'ปรับเปลี่ยนสถานะ' จะกลับมาสวยงามครับ");
            System.out.println("⚠️ อย่าลืม: ปิดโปรแกรม DB Browser ก่อนกดจองนะครับ ไม่งั้นจะ Error");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}