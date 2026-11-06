package main.ui.admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.service.AuthService;
import main.service.DatabaseConnector;
import main.ui.user.StartBooking;
import main.ui.user.StartBooking.CircleButton;

public class CalendarUI extends JFrame {
    private String adminUser;
    private JLabel lblMonthYear;
    private JPanel daysPanel;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private Calendar currentCalendar;

    public CalendarUI(String username) {
        this.adminUser = username;
        this.currentCalendar = Calendar.getInstance();
        
        ImageIcon windowIcon = new ImageIcon("images/logo.png");
        this.setIconImage(windowIcon.getImage());

        setTitle("ปฏิทินการจอง - Admin: " + adminUser);
        setSize(1100, 750); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // Panel หลักสำหรับจัดวาง Component ด้านใน
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);
        bg.add(mainPanel, BorderLayout.CENTER);

        // =========================================================
        // Header Section (ปรับเป็น null layout เพื่อวาง Logo และ Back)
        // =========================================================
        JPanel header = new JPanel(null);
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(1100, 100)); // กำหนดความสูงให้พอดีกับ Logo
        
        // 1. ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(10, 25, 50, 50);
        btnBack.addActionListener(e -> {
            new AdminMainMenu(adminUser).setVisible(true); 
            dispose();
        });
        header.add(btnBack);

        // 2. โลโก้ SUT (ขยับ x มาที่ 70 เพื่อไม่ให้ทับปุ่ม Back)
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int logoW = 120;
        int logoH = (int) ((double) logoIcon.getIconHeight() / logoIcon.getIconWidth() * logoW);
        Image scaledLogo = logoIcon.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(scaledLogo));
        lblLogo.setBounds(70, 10, logoW, logoH);
        header.add(lblLogo);

        // 3. ส่วนควบคุมเดือน (อยู่ตรงกลาง)
        JPanel monthControl = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        monthControl.setOpaque(false);
        monthControl.setBounds(200, 25, 700, 50);

        JButton btnPrev = new JButton("<");
        styleMonthButton(btnPrev);
        btnPrev.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            renderCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Leelawadee UI", Font.BOLD, 30));

        JButton btnNext = new JButton(">");
        styleMonthButton(btnNext);
        btnNext.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        monthControl.add(btnPrev);
        monthControl.add(lblMonthYear);
        monthControl.add(btnNext);
        header.add(monthControl);
        
        // บังคับลำดับให้ปุ่ม Back อยู่บนสุด
        header.setComponentZOrder(btnBack, 0);
        
        mainPanel.add(header, BorderLayout.NORTH);

        // =========================================================
        // Calendar Section (คงเดิมตามโค้ดของคุณ)
        // =========================================================
        JPanel calendarContainer = new JPanel(new BorderLayout(0, 10));
        calendarContainer.setBackground(Color.WHITE);
        calendarContainer.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));

        JPanel weeksPanel = new JPanel(new GridLayout(1, 7));
        weeksPanel.setBackground(new Color(245, 245, 245));
        String[] weeks = {"อาทิตย์", "จันทร์", "อังคาร", "พุธ", "พฤหัสบดี", "ศุกร์", "เสาร์"};
        for (String w : weeks) {
            JLabel lbl = new JLabel(w, SwingConstants.CENTER);
            lbl.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
            lbl.setPreferredSize(new Dimension(0, 40));
            weeksPanel.add(lbl);
        }
        calendarContainer.add(weeksPanel, BorderLayout.NORTH);

        daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        calendarContainer.add(daysPanel, BorderLayout.CENTER);

        mainPanel.add(calendarContainer, BorderLayout.CENTER);

        // =========================================================
        // Booking Detail Section (คงเดิมตามโค้ดของคุณ)
        // =========================================================
        String[] cols = {"รหัส", "ผู้จอง", "ห้อง", "เวลา", "อุปกรณ์", "สถานะ"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        bookingTable.setRowHeight(35);
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        
        JScrollPane scroll = new JScrollPane(bookingTable);
        scroll.setPreferredSize(new Dimension(1024, 200));
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), " รายการจองและอุปกรณ์ในวันที่เลือก ", 
            0, 0, new Font("Leelawadee UI", Font.BOLD, 14)));
        
        mainPanel.add(scroll, BorderLayout.SOUTH);

        renderCalendar();
    }

    private void renderCalendar() {
        daysPanel.removeAll();
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        
        String[] months = {"มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", 
                           "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"};
        lblMonthYear.setText(months[month] + " " + (year + 543));

        int startDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
        for (int i = 0; i < startDay; i++) {
            daysPanel.add(new JLabel(""));
        }

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            final int day = i;
            JButton btnDay = new JButton(String.valueOf(i));
            btnDay.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
            btnDay.setFocusPainted(false);
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            boolean hasBooking = checkBookingExists(year, month, day);
            
            if (hasBooking) {
                btnDay.setBackground(new Color(242, 101, 34)); 
                btnDay.setForeground(Color.RED); 
                btnDay.setToolTipText("มีการจองในวันนี้");
            } else {
                btnDay.setBackground(new Color(250, 250, 250));
                btnDay.setForeground(Color.BLACK);
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            }

            btnDay.addActionListener(e -> loadBookingByDate(year, month, day));
            daysPanel.add(btnDay);
        }
        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private boolean checkBookingExists(int year, int month, int day) {
        String dateStr = String.format("%04d-%02d-%02d", year, month + 1, day);
        String sql = "SELECT COUNT(*) FROM bookings WHERE start_time LIKE ? AND status = 'อนุมัติแล้ว'";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dateStr + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void loadBookingByDate(int y, int m, int d) {
        tableModel.setRowCount(0);
        String dateStr = String.format("%04d-%02d-%02d", y, m + 1, d);
        
        String sql = """
            SELECT b.*, 
            (SELECT GROUP_CONCAT(equipment_name || '(' || quantity || ')') 
             FROM booking_equipment 
             WHERE booking_id = b.booking_id) as equip_list
            FROM bookings b 
            WHERE b.start_time LIKE ? AND b.status = 'อนุมัติแล้ว'
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dateStr + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");
                String time = (start.contains("T") ? start.split("T")[1] : start) + " - " + 
                              (end.contains("T") ? end.split("T")[1] : end);
                
                String equips = rs.getString("equip_list");
                if (equips == null || equips.isEmpty()) equips = "-"; 
                
                tableModel.addRow(new Object[]{
                    rs.getInt("booking_id"),
                    rs.getString("username"),
                    AuthService.mapRoomIdToName(rs.getInt("room_id")),
                    time,
                    equips, 
                    rs.getString("status")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void styleMonthButton(JButton btn) {
        btn.setFont(new Font("Tahoma", Font.BOLD, 20));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}