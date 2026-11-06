package main;

import javax.swing.SwingUtilities;

import main.service.DatabaseConnector;

/**
 * นี่คือ "จุดเริ่มต้น" ใหม่ของโปรแกรม
 * ต่อไปนี้ ให้คุณกด "Run" ที่ไฟล์นี้ (แทน MainUI.java)
 */
public class MainApp {

    public static void main(String[] args) {
        
        // 1. สั่งให้เตรียมฐานข้อมูลก่อนเป็นอย่างแรก
        // (มันจะสร้างไฟล์ .db และตารางให้เราอัตโนมัติ)
        DatabaseConnector.initializeDatabase();

        // 2. พอฐานข้อมูลพร้อม ค่อยเปิดหน้าจอ Login
        SwingUtilities.invokeLater(() -> {
            MainUI frame = new MainUI();
            frame.setVisible(true);
        });
    }
}