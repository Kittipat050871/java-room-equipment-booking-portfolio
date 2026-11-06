package main.ui.admin;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.model.Booking;
import main.service.AuthService;
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton; 

public class RequestApproveFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private AuthService authService;
    private String adminUser;

    public RequestApproveFrame(String username) {
    	this.adminUser = username;
        authService = new AuthService();

        // ตั้งค่าหน้าจอ
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());
        
        setTitle("อนุมัติคำขอจองห้อง");
        setSize(1024, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // =========================================================
        // Header
        // =========================================================
        JPanel headerPanel = new JPanel(null);
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1024, 100));

        // ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 25, 50, 50);
        btnBack.addActionListener(e -> {
        	new ManageBookingMenu(adminUser).setVisible(true); 
            dispose();
        });
        headerPanel.add(btnBack);

        // Logo & Title
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 100;
        int newHeight = (int) ((double) logoIcon.getIconHeight() / logoIcon.getIconWidth() * newWidth);
        Image scaled = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaled));
        logo.setBounds(100, 15, newWidth, newHeight);
        headerPanel.add(logo);

        JLabel title = new JLabel("รายการขอจอง (รออนุมัติ)");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        title.setBounds(220, 35, 400, 40);
        headerPanel.add(title);

        bg.add(headerPanel, BorderLayout.NORTH);

        // =========================================================
        // Table (เต็มกรอบ)
        // =========================================================
        String[] columns = {"ID", "ผู้จอง", "ห้อง", "วันที่", "เวลา", "วัตถุประสงค์", "อุปกรณ์", "สถานะ"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.setFillsViewportHeight(true);
        
        // จัดกลางข้อมูล
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0)); 
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        bg.add(scrollPane, BorderLayout.CENTER);

        // =========================================================
        // Buttons
        // =========================================================
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(1024, 80));
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));

        // ปุ่มสีพิเศษ
        JButton btnApprove = new ColorShadowButton("อนุมัติ (Approve)", new Color(37, 99, 235));
        JButton btnReject = new ColorShadowButton("ไม่อนุมัติ (Reject)", new Color(220, 38, 38));

        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        bg.add(btnPanel, BorderLayout.SOUTH);

        // ===== Action Listeners =====
        btnApprove.addActionListener(e -> processRequest("อนุมัติแล้ว"));
        btnReject.addActionListener(e -> processRequest("ไม่อนุมัติ"));

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        model.setRowCount(0);
        List<Booking> list = authService.getPendingRequests(); 

        for (Booking b : list) {
            model.addRow(new Object[]{
                b.getBookingId(),
                b.getStudentId(),
                b.getRoomName(),
                b.getDate(),
                b.getTimeRange(),
                b.getPurpose(),
                b.getEquipments(),
                b.getStatus()
            });
        }
    }

    private void processRequest(String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "กรุณาเลือกรายการที่ต้องการจัดการ", "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (int) model.getValueAt(selectedRow, 0); 
        
        // Double Booking Check
        if (newStatus.contains("อนุมัติ")) {
            if (isDoubleBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, 
                    "❌ ไม่สามารถอนุมัติได้!\nเนื่องจากช่วงเวลานี้มีการจองอื่นที่อนุมัติไปแล้ว", 
                    "เวลาชนกัน", 
                    JOptionPane.ERROR_MESSAGE);
                return; 
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "ยืนยันการเปลี่ยนสถานะเป็น: " + newStatus + "?", 
                "ยืนยัน", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // เรียกอัปเดตสถานะใน DB
            if (authService.updateBookingStatus(bookingId, newStatus)) {
                
                // [จุดที่เพิ่มใหม่] ส่งแจ้งเตือนหา User ทันทีที่อัปเดตสำเร็จ
                authService.sendBookingAlert(bookingId, newStatus); 
                
                JOptionPane.showMessageDialog(this, "ดำเนินการเรียบร้อย และแจ้งเตือนผู้ใช้แล้ว");
                loadPendingRequests(); // รีเฟรชตาราง
            } else {
                JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการอัปเดต", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean isDoubleBooking(int bookingId) {
        Booking b = authService.getBookingById(bookingId);
        if (b == null) return true;

        String sql = "SELECT room_id, start_time, end_time FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookingId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int roomId = rs.getInt("room_id");
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");
                return !authService.isRoomAvailable(roomId, start, end, bookingId);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Class ปุ่มพิเศษ
    class ColorShadowButton extends JButton {
        private Color baseColor;
        private Color shadowColor = new Color(0, 0, 0, 50); 

        public ColorShadowButton(String text, Color bg) {
            super(text);
            this.baseColor = bg;
            
            setFont(new Font("Leelawadee UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(200, 50));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = 4;
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth()-shadowOffset, getHeight()-shadowOffset, 20, 20);

            if (getModel().isPressed()) {
                g2.setColor(baseColor.darker());
                g2.translate(2, 2); 
            } else if (getModel().isRollover()) {
                g2.setColor(baseColor.brighter());
            } else {
                g2.setColor(baseColor);
            }
            
            g2.fillRoundRect(0, 0, getWidth()-shadowOffset, getHeight()-shadowOffset, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}