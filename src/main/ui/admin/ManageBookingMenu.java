package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.ui.user.RoomPlan5.CircleButton;

import java.io.File;

public class ManageBookingMenu extends JFrame {
	private String adminUser;
	private String username;
	

    public ManageBookingMenu(String username) {
    	this.adminUser = username;
    	
    	try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font thaiFont = new Font("Leelawadee UI", Font.PLAIN, 14);
            UIManager.put("OptionPane.messageFont", thaiFont);
            UIManager.put("OptionPane.buttonFont", thaiFont);
        } catch (Exception e) { e.printStackTrace(); }
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("จัดการระบบการจอง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Background
        MainMenu.BackgroundPanel bg = new MainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== ปุ่มย้อนกลับ =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            // 3. ส่งชื่อกลับไปหน้าหลัก Admin
            new AdminMainMenu(adminUser).setVisible(true); 
            dispose();
        });

        // ===== โลโก้ =====
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        if (originalWidth <= 0) originalWidth = 1; 
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel logo = new JLabel(scaledIcon);
        logo.setBounds(70, 10, newWidth, newHeight);
        bg.add(logo);

        // ===== หัวข้อ =====
        JLabel title = new JLabel("จัดการระบบการจอง");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(Color.BLACK);
        title.setBounds(230, 35, 600, 40);
        bg.add(title);

        // ===== ปุ่มเมนู (ขาว + เงา + รูปบน + ข้อความล่าง) =====
        Font btnFont = new Font("Leelawadee UI", Font.BOLD, 20);

        JButton btnRequest = createShadowButton("คำขอ / อนุมัติ", "images/Approve.png", btnFont);
        JButton btnEquipStatus = createShadowButton("ปรับเปลี่ยนสถานะอุปกรณ์", "images/Change.png", btnFont);

        // ** ปรับขนาดปุ่มให้สูงขึ้น เพื่อรองรับแนวตั้ง **
        int w = 350; 
        int h = 150; // เพิ่มความสูง (ของเดิม 100)
        
        int centerX = (1024 - w) / 2;
        int y1 = 130;
        int y2 = 300; // ขยับห่างกันหน่อย (180 + 150 + 20)

        btnRequest.setBounds(centerX, y1, w, h);
        btnEquipStatus.setBounds(centerX, y2, w, h);

        bg.add(btnRequest);
        bg.add(btnEquipStatus);

        // ===== Action =====
        btnRequest.addActionListener(e -> {
        	new RequestApproveFrame(adminUser).setVisible(true); // ส่ง adminUser ไป
            dispose();
        });

        btnEquipStatus.addActionListener(e -> {
        	new EquipStatusFrame(adminUser).setVisible(true); // ส่ง adminUser ไป
            dispose();
        });
    }

    // =================================================================
    // Method สร้างปุ่ม (แบบแนวตั้ง - รูปบน ข้อความล่าง)
    // =================================================================
    private JButton createShadowButton(String text, String iconPath, Font font) {
        ModernShadowButton btn = new ModernShadowButton(text);
        btn.setFont(font);
        btn.setForeground(Color.BLACK); 

        // ใส่รูป
        ImageIcon icon = new ImageIcon(iconPath);
        if (icon.getIconWidth() > 0) {
            int iconSize = 60; // รูปใหญ่ขึ้นนิดนึงให้สมดุล
            Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        }

        // --- จัดตำแหน่ง (เหมือน MainMenu) ---
        // 1. ให้เนื้อหาทั้งหมดกองที่ "ด้านล่าง" (เพื่อให้ข้อความอยู่ระนาบเดียวกัน)
        btn.setVerticalAlignment(SwingConstants.BOTTOM);
        
        // 2. จัดแนวนอนกึ่งกลาง
        btn.setHorizontalAlignment(SwingConstants.CENTER);

        // 3. รูปอยู่บน ข้อความอยู่ล่าง
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        // 4. ระยะห่าง
        btn.setIconTextGap(10); 

        // 5. ดันข้อความขึ้นจากขอบล่าง 15px (Padding)
        // (บน, ซ้าย, ล่าง, ขวา)
        btn.setBorder(new EmptyBorder(5, 5, 15, 5)); 

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =================================================================
    // Class ปุ่มมีเงา (เหมือนเดิม)
    // =================================================================
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageBookingMenu("admin").setVisible(true));//แดงส่วนนี้
    }
}