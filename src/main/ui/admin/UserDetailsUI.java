package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu.BackgroundPanel;
import main.service.AuthService;
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton;

public class UserDetailsUI extends JFrame {

    private String currentUser;

    public UserDetailsUI(String username) {
        this.currentUser = username;

        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("รายละเอียดผู้ใช้: " + username);
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // =========================================================
        // Header Section
        // =========================================================
        JPanel headerPanel = new JPanel(null);
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1024, 100));

        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 25, 50, 50);
        btnBack.addActionListener(e -> {
            new UserStatusUI(currentUser).setVisible(true); 
            dispose();
        });
        headerPanel.add(btnBack);

        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth() > 0 ? logoIcon.getIconWidth() : 1;
        int newHeight = (int) ((double) logoIcon.getIconHeight() / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(90, 10, newWidth, newHeight);
        headerPanel.add(logo);

        JLabel title = new JLabel("รายละเอียดการจอง");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 36));
        title.setForeground(new Color(60, 60, 60)); 
        title.setBounds(260, 30, 500, 45);
        headerPanel.add(title);

        headerPanel.setComponentZOrder(btnBack, 0);
        bg.add(headerPanel, BorderLayout.NORTH);

        // =========================================================
        // Content Area
        // =========================================================
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel infoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoWrapper.setOpaque(false);
        
        String[] contactInfo = getUserContactInfo(username);
        String phone = contactInfo[0];
        String email = contactInfo[1];
        
        UserInfoBadge userBadge = new UserInfoBadge("ชื่อผู้ใช้ " + username + "    เบอร์ " + phone + "    อีเมล " + email);
        userBadge.setPreferredSize(new Dimension(850, 50)); 
        infoWrapper.add(userBadge);
        
        contentPanel.add(infoWrapper, BorderLayout.NORTH);

        JPanel cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        cardsContainer.setOpaque(false);

        loadBookingCards(cardsContainer);

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(scroll, BorderLayout.CENTER);
        bg.add(contentPanel, BorderLayout.CENTER);
    }

    private String[] getUserContactInfo(String user) {
        String[] info = {"-", "-"}; 
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT phone, email FROM users WHERE username = ?")) {
            pstmt.setString(1, user);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                info[0] = rs.getString("phone") != null ? rs.getString("phone") : "-";
                info[1] = rs.getString("email") != null ? rs.getString("email") : "-";
            }
        } catch (Exception e) { e.printStackTrace(); }
        return info;
    }

    private void loadBookingCards(JPanel container) {
        String sql = "SELECT b.*, r.room_name, " +
                     "(SELECT GROUP_CONCAT(equipment_name || ' x' || quantity, ', ') FROM booking_equipment WHERE booking_id = b.booking_id) as equipments " +
                     "FROM bookings b LEFT JOIN rooms r ON b.room_id = r.room_id WHERE b.username = ? GROUP BY b.booking_id ORDER BY b.start_time DESC";
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("booking_id");
                String start = rs.getString("start_time");
                String end = rs.getString("end_time");
                String dateStr = (start != null && start.contains("T")) ? start.split("T")[0] : start;
                String timeRange = (start != null && start.contains("T") ? start.split("T")[1] : start) + " - " + 
                                  (end != null && end.contains("T") ? end.split("T")[1] : end);
                String room = rs.getString("room_name");
                if (room == null) room = AuthService.mapRoomIdToName(rs.getInt("room_id"));
                
                container.add(new BookingCard(id, dateStr, room, rs.getString("equipments"), rs.getString("purpose"), timeRange, rs.getString("status")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================
    // Custom Components
    // =========================================================

    class UserInfoBadge extends JLabel {
        public UserInfoBadge(String text) {
            super(text);
            setFont(new Font("Leelawadee UI", Font.BOLD, 16));
            setHorizontalAlignment(CENTER);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class BookingCard extends JPanel {
        public BookingCard(int bookingId, String date, String room, String equip, String purpose, String time, String status) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(280, 360)); 
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 20, 20, 20)); 

            JLabel lblDate = new JLabel(date);
            lblDate.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
            lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(lblDate);
            add(Box.createVerticalStrut(15));

            // แก้ไขส่วนรายละเอียดให้ใช้ JTextArea เพื่อไม่ให้ข้อความขาด
            add(createDetailLine("ห้อง:", room));
            add(Box.createVerticalStrut(5));
            add(createDetailLine("อุปกรณ์:", equip)); 
            add(Box.createVerticalStrut(5));
            add(createDetailLine("วัตถุประสงค์:", purpose));
            add(Box.createVerticalStrut(5));
            add(createDetailLine("เวลา:", time));
            
            add(Box.createVerticalGlue()); // ดันสถานะและปุ่มลงล่าง

            JLabel lblStatus = new JLabel("สถานะ: " + status);
            lblStatus.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
            lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
            if (status.contains("อนุมัติแล้ว")) lblStatus.setForeground(new Color(0, 150, 0));
            else if (status.contains("ไม่用户อนุมัติ") || status.contains("ไม่อนุมัติ")) lblStatus.setForeground(Color.RED);
            else lblStatus.setForeground(new Color(150, 150, 150));
            add(lblStatus);

            add(Box.createVerticalStrut(10));

            // เพิ่มปุ่ม "ดูรายละเอียด" เพื่อดูข้อมูลฉบับเต็ม
            JButton btnFullDetails = new JButton("ดูรายละเอียด");
            btnFullDetails.setFont(new Font("Leelawadee UI", Font.PLAIN, 11));
            btnFullDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnFullDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnFullDetails.addActionListener(e -> {
                String fullMsg = String.format("วันที่: %s\nห้อง: %s\nอุปกรณ์: %s\nวัตถุประสงค์: %s\nเวลา: %s\nสถานะ: %s",
                                 date, room, equip, (purpose == null || purpose.isEmpty() ? "-" : purpose), time, status);
                JOptionPane.showMessageDialog(this, fullMsg, "รายละเอียดการจอง", JOptionPane.INFORMATION_MESSAGE);
            });
            add(btnFullDetails);
        }

        // เมธอดสร้างบรรทัดรายละเอียดที่รองรับข้อความยาว
        private JPanel createDetailLine(String label, String value) {
            JPanel line = new JPanel(new BorderLayout(5, 0));
            line.setOpaque(false);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel l = new JLabel(label);
            l.setFont(new Font("Leelawadee UI", Font.BOLD, 13));
            l.setForeground(new Color(100, 100, 100));
            l.setVerticalAlignment(SwingConstants.TOP);
            
            JTextArea v = new JTextArea(value == null || value.isEmpty() ? "-" : value);
            v.setFont(new Font("Leelawadee UI", Font.PLAIN, 13));
            v.setLineWrap(true);
            v.setWrapStyleWord(true);
            v.setEditable(false);
            v.setOpaque(false);
            v.setFocusable(false);
            
            line.add(l, BorderLayout.WEST);
            line.add(v, BorderLayout.CENTER);
            return line;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);
            g2.dispose();
        }
    }
}