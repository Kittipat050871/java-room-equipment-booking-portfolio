package main.ui.admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import main.service.DatabaseHelper;
import main.ui.user.RoomPlanMenu;
import main.ui.user.RoomPlan5.CircleButton;

import java.awt.*;
import java.sql.*;

public class AllBookingsFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel model;
    private JTable table;

    public AllBookingsFrame() {

        setTitle("รายการการจองทั้งหมด");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        AdminMainMenu.BackgroundPanel bg = new AdminMainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

     // ===== ปุ่มย้อนกลับ =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new RoomPlanMenu().setVisible(true);
            dispose();
        });

        // ===== Title =====
        JLabel title = new JLabel("รายการการจองทั้งหมด");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(Color.BLACK);
        title.setBounds(260, 35, 600, 40);
        bg.add(title);

        // ===== Table =====
        String[] columns = {"เลขที่", "ผู้จอง", "ห้อง", "วันที่", "เวลา", "สถานะ"};
        model = new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // ห้ามแก้ไขในตาราง
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        table.setRowHeight(28);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(80, 120, 860, 370);
        bg.add(scroll);

        // โหลดข้อมูลจาก DB
        loadAllBookingData();
    }

    // ===== ดึงข้อมูลทั้งหมดจาก DB =====
    private void loadAllBookingData() {
        model.setRowCount(0); // ล้างตารางก่อน

        String sql = "SELECT * FROM booking_requests ORDER BY date DESC, time DESC";

        try (Connection conn = DatabaseHelper.getConnection();//แดงตัวนี้ หลังย้ายDatabaseHelper
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("room"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("status")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการโหลดข้อมูล", 
                                          "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

}