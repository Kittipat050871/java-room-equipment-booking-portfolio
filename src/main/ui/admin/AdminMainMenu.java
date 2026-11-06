package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// นำเข้า MainUI จากแพ็กเกจหลักเพื่อใช้ในฟังก์ชัน Logout
import main.MainUI;

public class AdminMainMenu extends JFrame {
    private String adminUser;

    public AdminMainMenu(String username) {
        this.adminUser = username;

        //ตั้งค่า Icon ของหน้าต่างโปรแกรม
        try {
            ImageIcon appIcon = new ImageIcon("images/logo.png");
            this.setIconImage(appIcon.getImage());
        } catch (Exception e) {
            System.out.println("ไม่สามารถโหลด Icon หน้าต่างได้");
        }

        setTitle("หน้าหลักผู้ดูแลระบบ - แอดมิน: " + adminUser);
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // พื้นหลังหลัก
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);
        
        // =========================================================
        // ส่วนของโลโก้ (แก้ไข: เพิ่ม bg.add เพื่อให้แสดงผล)
        // =========================================================
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150; 
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        if (originalWidth <= 0) originalWidth = 1; 
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logo = new JLabel(scaledIcon); 
        logo.setBounds(145, 40, newWidth, newHeight); 
        bg.add(logo);

        // =========================================================
        // หัวข้อระบบ
        // =========================================================
        JLabel title = new JLabel("ระบบจองห้องประชุมและอุปกรณ์ (ผู้ดูแล)"); 
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(Color.BLACK); 
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(260, 70, 600, 40);
        bg.add(title);

        // =========================================================
        // ปุ่มเมนู
        // =========================================================
        Font f = new Font("Leelawadee UI", Font.BOLD, 18);

        JButton b1 = createShadowButton("จัดการระบบการจอง", "images/Manage System.png", f);
        JButton b2 = createShadowButton("จัดการการแจ้งเตือน", "images/Bell.png", f);
        JButton b3 = createShadowButton("สถานะผู้ใช้งาน", "images/Account.png", f);
        JButton b6 = createShadowButton("สถิติการใช้งาน", "images/Stats.png", f);
        JButton b5 = createShadowButton("ปฏิทินการจอง", "images/Calendar.png", f); 
        JButton b4 = createShadowButton("ออกจากระบบ", "images/Logout.png", f);

        // กำหนดตำแหน่ง 3 แถว แถวละ 2 ปุ่ม
        int w = 230, h = 100;
        int leftX = 272, rightX = 522;

        b1.setBounds(leftX, 160, w, h);  b2.setBounds(rightX, 160, w, h); 
        b3.setBounds(leftX, 280, w, h);  b6.setBounds(rightX, 280, w, h); 
        b5.setBounds(leftX, 400, w, h);  b4.setBounds(rightX, 400, w, h); 

        bg.add(b1); bg.add(b2); bg.add(b3);
        bg.add(b4); bg.add(b5); bg.add(b6);

        // =========================================================
        // Action Listeners (เชื่อมโยงไปยังหน้าอื่นๆ ในแพ็กเกจ admin)
        // =========================================================
        b1.addActionListener(e -> { new ManageBookingMenu(adminUser).setVisible(true); dispose(); });
        b2.addActionListener(e -> { new ManageAlertUI(adminUser).setVisible(true); dispose(); });
        b3.addActionListener(e -> { new UserStatusUI(adminUser).setVisible(true); dispose(); });
        b4.addActionListener(e -> { new MainUI().setVisible(true); dispose(); }); // กลับไปหน้า Login
        b5.addActionListener(e -> { new CalendarUI(adminUser).setVisible(true); dispose(); });
        b6.addActionListener(e -> { new StatsUI(adminUser).setVisible(true); dispose(); });
    }

    private JButton createShadowButton(String text, String iconPath, Font font) {
        ModernShadowButton btn = new ModernShadowButton(text);
        btn.setFont(font);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // คลาสปุ่มแบบมีเงา
    public static class ModernShadowButton extends JButton {
        public ModernShadowButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // วาดเงา
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(3, 3, getWidth()-3, getHeight()-3, 20, 20);
            // วาดตัวปุ่ม
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // คลาสจัดการพื้นหลัง
    public static class BackgroundPanel extends JPanel {
        private Image img = new ImageIcon("images/พื้นหลัง.png").getImage();
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }
}