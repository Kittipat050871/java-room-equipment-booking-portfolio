package main;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.model.User;
import main.service.AuthService;
import main.service.DatabaseConnector;
import main.ui.user.AccountUI;
import main.ui.user.HistoryUI;
import main.ui.user.NotificationUI;
import main.ui.user.StartBooking;
import main.ui.admin.EquipmentListUI; // [สำคัญ] อย่าลืม import หน้านี้

public class MainMenu extends JFrame {

    private Timer statusTimer; 
    private JPanel alertPanel;
    private BadgeButton b5; 

    public MainMenu() {
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("ระบบจองห้องประชุมและอุปกรณ์");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout(); 
            }
        });

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===========================================
        // 1. ส่วนแสดงผล: โลโก้ + ชื่อระบบ + ยินดีต้อนรับ
        // ===========================================
        
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 120; 
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        if (originalWidth <= 0) originalWidth = 1; 
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage)); 
        logo.setBounds(80, 30, newWidth, newHeight); 
        bg.add(logo);

        JLabel systemTitle = new JLabel("ระบบจองห้องประชุมและอุปกรณ์");
        systemTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 28));
        systemTitle.setForeground(Color.BLACK); 
        systemTitle.setHorizontalAlignment(SwingConstants.LEFT);
        systemTitle.setBounds(220, 45, 600, 40);
        bg.add(systemTitle);

        // ดึงชื่อผู้ใช้มาโชว์
        AuthService auth = new AuthService();
        User currentUser = auth.getUserDetails(AuthService.currentLoggedInStudentId);
        
        String showName = AuthService.currentLoggedInStudentId; 
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) {
            showName = currentUser.getName(); 
        }

        JLabel welcomeLabel = new JLabel("ยินดีต้อนรับ, " + showName);
        welcomeLabel.setFont(new Font("Leelawadee UI", Font.PLAIN, 20)); 
        welcomeLabel.setForeground(new Color(80, 80, 80)); 
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        welcomeLabel.setBounds(220, 85, 600, 30); 
        bg.add(welcomeLabel);

        // =================================================================
        // 2. ปุ่มเมนู (Buttons)
        // =================================================================
        Font f = new Font("Leelawadee UI", Font.BOLD, 18);

        // ปุ่มที่ 1: เริ่มต้นการจอง
        JButton b1 = createShadowButton("เริ่มต้นการจอง", "images/Start.png", f);
        
        // [แก้ไข] ปุ่มที่ 2: เปลี่ยนจาก "ระเบียบการใช้" เป็น "รายการอุปกรณ์"
        // เพื่อให้ User เข้าไป Sort & Search อุปกรณ์ได้
        JButton b2 = createShadowButton("รายการอุปกรณ์", "images/list.png", f);
        
        JButton b3 = createShadowButton("บัญชีของฉัน", "images/Account.png", f);
        JButton b4 = createShadowButton("ประวัติการจอง", "images/History.png", f);
        
        // ปุ่มที่ 5: การแจ้งเตือน (มีจุดแดง)
        b5 = new BadgeButton("การแจ้งเตือน");
        b5.setFont(f);
        b5.setForeground(Color.BLACK);
        ImageIcon iconB5 = new ImageIcon("images/Bell.png");
        if (iconB5.getIconWidth() > 0) {
             int iconSize = 60; 
             Image img = iconB5.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
             b5.setIcon(new ImageIcon(img));
        }
        b5.setVerticalAlignment(SwingConstants.BOTTOM);
        b5.setHorizontalAlignment(SwingConstants.CENTER);
        b5.setVerticalTextPosition(SwingConstants.BOTTOM);
        b5.setHorizontalTextPosition(SwingConstants.CENTER);
        b5.setIconTextGap(5); 
        b5.setBorder(new EmptyBorder(5, 5, 15, 5)); 
        b5.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JButton b6 = createShadowButton("ออกจากระบบ", "images/Logout.png", f);

        int w = 230, h = 120;
        int y1 = 160;
        int y2 = 300; 

        b1.setBounds(150, y1, w, h);
        b2.setBounds(397, y1, w, h);
        b3.setBounds(644, y1, w, h);
        b4.setBounds(150, y2, w, h);
        b5.setBounds(397, y2, w, h);
        b6.setBounds(644, y2, w, h);

        bg.add(b1); bg.add(b2); bg.add(b3);
        bg.add(b4); bg.add(b5); bg.add(b6);
        
        // --- Action Listeners ---
        b1.addActionListener(e -> { new StartBooking().setVisible(true); this.dispose(); });
        
        // [แก้ไข Action] กดปุ่มนี้แล้วเปิดหน้า รายการอุปกรณ์ (EquipmentListUI)
        b2.addActionListener(e -> { 
            // เปิดหน้าจอแยกขึ้นมา ไม่ต้องปิดหน้าหลัก (User จะได้กดดูแล้วปิดกลับมาจองต่อได้สะดวก)
            new EquipmentListUI().setVisible(true); 
        });
        
        b3.addActionListener(e -> { new AccountUI().setVisible(true); this.dispose(); });
        b4.addActionListener(e -> { new HistoryUI().setVisible(true); this.dispose(); });
        b5.addActionListener(e -> { new NotificationUI().setVisible(true); this.dispose(); });
        b6.addActionListener(e -> { handleLogout(); });

        // =================================================================
        // 3. แจ้งเตือน (Notifications System)
        // =================================================================
        alertPanel = new JPanel();
        alertPanel.setOpaque(false);
        alertPanel.setBounds(150, 440, 724, 110); 
        // bg.add(alertPanel); 

        updateNotifications();

        statusTimer = new Timer(5000, e -> {
            updateNotifications();
        });
        statusTimer.start();
    }

    private void updateNotifications() {
        loadUserAlerts(alertPanel);
        AuthService auth = new AuthService();
        String currentUser = AuthService.currentLoggedInStudentId;
        
        if (currentUser != null && !currentUser.isEmpty()) {
            int unreadCount = auth.getUnreadCount(currentUser);
            b5.setBadgeCount(unreadCount);
        } else {
            b5.setBadgeCount(0);
        }
    }

    private void handleLogout() {
        if (statusTimer != null) statusTimer.stop();
        updateUserStatus(AuthService.currentLoggedInStudentId, "Inactive");
        this.dispose();
        new main.MainUI().setVisible(true);
    }

    private void updateUserStatus(String username, String status) {
        if (username == null || username.isEmpty()) return;
        String sql = "UPDATE users SET status = ? WHERE username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserAlerts(JPanel containerPanel) {
        if (containerPanel == null) return;
        containerPanel.removeAll();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        
        String currentUser = AuthService.currentLoggedInStudentId;
        String sql = "SELECT * FROM alerts WHERE target_user = 'ALL' OR target_user = ? ORDER BY id DESC LIMIT 2"; 
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, currentUser);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String date = rs.getString("alert_date");
                String msg = rs.getString("message");
                String target = "ALL";
                try { target = rs.getString("target_user"); } catch (Exception e) {}
                
                String htmlText;
                if (!"ALL".equals(target)) {
                    htmlText = String.format("<html><span style='color:blue;'>[ถึงคุณ] • %s</span> : %s</html>", date, msg);
                } else {
                    htmlText = String.format("<html><span style='color:red;'>• %s</span> : %s</html>", date, msg);
                }

                JLabel lblAlert = new JLabel(htmlText);
                lblAlert.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
                lblAlert.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setOpaque(false);
                row.add(lblAlert);
                containerPanel.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        containerPanel.revalidate();
        containerPanel.repaint();
    }
    
    private JButton createShadowButton(String text, String iconPath, Font font) {
        ModernShadowButton btn = new ModernShadowButton(text);
        btn.setFont(font);
        btn.setForeground(Color.BLACK); 
        ImageIcon icon = new ImageIcon(iconPath);
        if (icon.getIconWidth() > 0) {
            int iconSize = 60; 
            Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        }
        btn.setVerticalAlignment(SwingConstants.BOTTOM);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setIconTextGap(5); 
        btn.setBorder(new EmptyBorder(5, 5, 15, 5)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static class ModernShadowButton extends JButton {
        private Color shadowColor = new Color(0, 0, 0, 50); 
        private int shadowOffset = 5; 
        private int arcSize = 20; 

        public ModernShadowButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(Color.WHITE); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);
            g2.setColor(new Color(230, 230, 230));
            g2.drawRoundRect(0, 0, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class BackgroundPanel extends JPanel {
        private ImageIcon backgroundImageIcon;
        public BackgroundPanel() {
            try {
                backgroundImageIcon = new ImageIcon("images/พื้นหลัง.png");
            } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImageIcon != null && backgroundImageIcon.getIconWidth() > 0) {
                g.drawImage(backgroundImageIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }

    static class BadgeButton extends ModernShadowButton {
        private int badgeCount = 0;

        public BadgeButton(String text) {
            super(text);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public void setBadgeCount(int count) {
            if (this.badgeCount != count) {
                this.badgeCount = count;
                this.revalidate();
                this.repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (badgeCount > 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 59, 48)); 
                
                int diameter = 24; 
                int x = getWidth() - diameter - 12; 
                int y = 8; 
                
                g2.fillOval(x, y, diameter, diameter);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Tahoma", Font.BOLD, 12));
                String countStr = badgeCount > 99 ? "99+" : String.valueOf(badgeCount);
                
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (diameter - fm.stringWidth(countStr)) / 2;
                int textY = y + (diameter + fm.getAscent()) / 2 - 2;
                
                g2.drawString(countStr, textX, textY);
                g2.dispose();
            }
        }
    }
}