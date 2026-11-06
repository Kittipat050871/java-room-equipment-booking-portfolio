package main.ui.user;

import javax.swing.*;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.model.Booking;
import main.service.AuthService;
import main.ui.user.RoomPlan5.CircleButton;

import java.awt.*;
import java.util.List;

public class BookingStatusUI extends JFrame {

    private JLabel lblUser;
    private JLabel lblRoom;
    private JLabel lblDate;
    private JLabel lblPurpose;
    private JLabel lblTime;
    private JLabel lblStatus;   // แสดงสถานะล่าสุด

    private AuthService authService;
    private Timer refreshTimer;   // timer เอาไว้ refresh ข้อมูล

    // ===== constructor เดียว ใช้งานทุกที่ =====
    public BookingStatusUI() {
        this.authService = new AuthService();

        initUI();          // สร้างหน้าจอ
        refreshBooking();  // โหลดข้อมูลครั้งแรก

        // ให้รีเฟรชจาก DB ทุก 5 วินาที (real-time คร่าว ๆ)
        refreshTimer = new Timer(5000, e -> refreshBooking());
        refreshTimer.start();
    }

    // ================== สร้างหน้าจอ ==================
    private void initUI() {
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("สรุปการจอง/สถานะการจอง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // พื้นหลังแบบเดียวกับหน้าอื่น
        MainMenu.BackgroundPanel bg = new MainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== ปุ่มย้อนกลับ =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new StartBooking().setVisible(true);
            dispose();
        });

        // ===== โลโก้ =====
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel logo = new JLabel(scaledIcon);
        logo.setBounds(70, 10, newWidth, newHeight);
        bg.add(logo);

        // ===== หัวข้อ =====
        JLabel title = new JLabel("สรุปการจอง/สถานะการจอง");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(Color.BLACK);
        title.setBounds(230, 30, 600, 50);
        bg.add(title);

        // ===== กล่องกลาง =====
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setBackground(new Color(245, 245, 245));
        card.setBounds(260, 140, 500, 340);
        bg.add(card);

        Font labelFont = new Font("Leelawadee UI", Font.BOLD, 20);
        Font valueFont = new Font("Leelawadee UI", Font.PLAIN, 20);

        int labelX = 60;
        int fieldX = 170;
        int labelW = 100;
        int fieldW = 300;
        int startY = 40;
        int gapY = 45;

        // 1. ชื่อผู้จอง
        JLabel l1 = new JLabel("ชื่อผู้จอง", SwingConstants.RIGHT);
        l1.setFont(labelFont);
        l1.setBounds(labelX, startY, labelW, 30);
        card.add(l1);

        lblUser = createLineLabel("-", valueFont);
        lblUser.setBounds(fieldX, startY, fieldW, 30);
        card.add(lblUser);

        // 2. ห้อง
        JLabel l2 = new JLabel("ห้อง", SwingConstants.RIGHT);
        l2.setFont(labelFont);
        l2.setBounds(labelX, startY + gapY, labelW, 30);
        card.add(l2);

        lblRoom = createLineLabel("-", valueFont);
        lblRoom.setBounds(fieldX, startY + gapY, fieldW, 30);
        card.add(lblRoom);

        // 3. วันที่
        JLabel l3 = new JLabel("วันที่", SwingConstants.RIGHT);
        l3.setFont(labelFont);
        l3.setBounds(labelX, startY + gapY * 2, labelW, 30);
        card.add(l3);

        lblDate = createLineLabel("-", valueFont);
        lblDate.setBounds(fieldX, startY + gapY * 2, fieldW, 30);
        card.add(lblDate);

        // 4. วัตถุประสงค์
        JLabel l4 = new JLabel("วัตถุประสงค์", SwingConstants.RIGHT);
        l4.setFont(labelFont);
        l4.setBounds(labelX, startY + gapY * 3, labelW, 30);
        card.add(l4);

        lblPurpose = createLineLabel("-", valueFont);
        lblPurpose.setBounds(fieldX, startY + gapY * 3, fieldW, 30);
        card.add(lblPurpose);

        // 5. เวลา
        JLabel l5 = new JLabel("เวลา", SwingConstants.RIGHT);
        l5.setFont(labelFont);
        l5.setBounds(labelX, startY + gapY * 4, labelW, 30);
        card.add(l5);

        lblTime = createLineLabel("-", valueFont);
        lblTime.setBounds(fieldX, startY + gapY * 4, fieldW, 30);
        card.add(lblTime);

        // 6. สถานะ
        JLabel l6 = new JLabel("สถานะ", SwingConstants.RIGHT);
        l6.setFont(labelFont);
        l6.setBounds(labelX, startY + gapY * 5, labelW, 30);
        card.add(l6);

        lblStatus = createLineLabel("-", valueFont);
        lblStatus.setBounds(fieldX, startY + gapY * 5, fieldW, 30);
        card.add(lblStatus);

        // ===== ปุ่มตกลง =====
        JButton btnOk = new JButton("ตกลง");
        btnOk.setFont(new Font("Leelawadee UI", Font.BOLD, 20));
        btnOk.setBounds(190, 295, 120, 35);
        card.add(btnOk);

        btnOk.addActionListener(e -> {
            if (refreshTimer != null) refreshTimer.stop();
            new StartBooking().setVisible(true);
            dispose();
        });
    }

    // label มีเส้นขีดด้านล่าง
    private JLabel createLineLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        return label;
    }

    // set ค่า field ทั้งหมด
    private void setValues(String user, String room, String date,
                           String purpose, String time, String status) {

        lblUser.setText(user != null && !user.isBlank() ? user : "-");
        lblRoom.setText(room != null && !room.isBlank() ? room : "-");
        lblDate.setText(date != null && !date.isBlank() ? date : "-");
        lblPurpose.setText(purpose != null && !purpose.isBlank() ? purpose : "-");
        lblTime.setText(time != null && !time.isBlank() ? time : "-");
        lblStatus.setText(status != null && !status.isBlank() ? status : "-");
    }

    // ดึง booking ล่าสุดของ user ที่ล็อกอินอยู่ แล้วอัปเดตหน้าจอ
    private void refreshBooking() {
        // เอา ID จาก AuthService (ต้อง set ตอน login แล้วนะ)
        String sid = AuthService.currentLoggedInStudentId;

        if (sid == null || sid.isBlank()) {
            // ยังไม่รู้ว่าใครล็อกอิน -> เคลียร์จอ
            setValues("-", "-", "-", "-", "-", "-");
            return;
        }

        List<Booking> history = authService.getBookingHistory(sid);
        if (!history.isEmpty()) {
            // สมมติ getBookingHistory(sid) คืนข้อมูลตามเวลาใหม่สุดอยู่ index 0
            Booking latest = history.get(0);

            setValues(
                    latest.getStudentId(),
                    latest.getRoomName(),
                    latest.getDate(),
                    latest.getPurpose(),
                    latest.getTimeRange(),
                    latest.getStatus()
            );
        } else {
            // ไม่มีประวัติการจองของคนนี้
            setValues(sid, "-", "-", "-", "-", "-");
        }
    }
}