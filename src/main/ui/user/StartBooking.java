package main.ui.user;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D; // สำหรับวาดปุ่มย้อนกลับ
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu;

import java.io.File;

public class StartBooking extends JFrame {

    public StartBooking() {

        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("เริ่มต้นการจอง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. Background
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // 2. ปุ่มย้อนกลับ (วงกลมสีส้ม)
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        // 3. โลโก้
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 120;
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        if (originalWidth <= 0) originalWidth = 1;
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);

        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logo = new JLabel(scaledIcon);
        logo.setBounds(100, 25, newWidth, newHeight);
        bg.add(logo);

        // 4. หัวข้อ "เริ่มต้นการจอง"
        JLabel title = new JLabel("เมนูการจองห้องประชุม");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(Color.BLACK);
        title.setBounds(240, 50, 400, 40);
        bg.add(title);

        // =================================================================
        // ส่วนปุ่มเมนู (Modern Shadow Buttons)
        // =================================================================
        Font f = new Font("Leelawadee UI", Font.BOLD, 18);

        // สร้างปุ่มทั้ง 5
        JButton btnBookRoom = createShadowButton("จองห้อง", "images/Room.png", f);
        JButton btnCalendar = createShadowButton("ปฏิทิน", "images/Start.png", f);
        JButton btnRules    = createShadowButton("ระเบียบการใช้", "images/Rules.png", f);
        JButton btnPlan     = createShadowButton("แผนผังห้อง", "images/Meeting Room.png", f);
        JButton btnStatus   = createShadowButton("สถานะการจอง", "images/Status.png", f);

        // กำหนดขนาดปุ่ม
        int w = 220;
        int h = 120;
        
        // จัดวางตำแหน่ง: แถวบน 3 ปุ่ม, แถวล่าง 2 ปุ่ม
        int row1Y = 140;
        int row2Y = 280;
        int gap = 30; // ช่องว่างระหว่างปุ่ม

        // คำนวณแกน X แถวบน (3 ปุ่ม)
        // (1024 - (3*220) - (2*30)) / 2 = เริ่มต้นที่ X เท่าไหร่...
        // เอาแบบ Fix สวยๆ เลย:
        int startX_Row1 = 152; 
        
        btnBookRoom.setBounds(startX_Row1, row1Y, w, h);
        btnCalendar.setBounds(startX_Row1 + w + gap, row1Y, w, h);
        btnRules.setBounds(startX_Row1 + (w + gap) * 2, row1Y, w, h);

        // คำนวณแกน X แถวล่าง (2 ปุ่ม - ให้อยู่กึ่งกลาง)
        int startX_Row2 = 277; // ขยับเข้ามาหน่อยให้ center กับแถวบน

        btnPlan.setBounds(startX_Row2, row2Y, w, h);
        btnStatus.setBounds(startX_Row2 + w + gap, row2Y, w, h);

        // เพิ่มปุ่มลงในจอ
        bg.add(btnBookRoom);
        bg.add(btnCalendar);
        bg.add(btnRules);
        bg.add(btnPlan);
        bg.add(btnStatus);

        // =================================================================
        // Action Listeners
        // =================================================================
        
        btnBookRoom.addActionListener(e -> {
            new BookingForm().setVisible(true);
            dispose();
        });

        btnCalendar.addActionListener(e -> {
            new BookingCalendar().setVisible(true);
            dispose();
        });

        btnRules.addActionListener(e -> {
            new RoomUsageRules().setVisible(true);
            dispose(); // เลือกเอาว่าจะปิดหน้านี้ไหม หรือแค่เปิดหน้าใหม่ซ้อน
        });

        btnPlan.addActionListener(e -> {
            new RoomPlanMenu().setVisible(true);
            dispose();
        });

        btnStatus.addActionListener(e -> {
            new BookingStatusUI().setVisible(true);
            dispose();
        });
    }

    // =================================================================
    // Method สร้างปุ่ม (แบบเดียวกับ MainMenu)
    // =================================================================
    private JButton createShadowButton(String text, String iconPath, Font font) {
        ModernShadowButton btn = new ModernShadowButton(text);
        btn.setFont(font);
        btn.setForeground(Color.BLACK); 

        ImageIcon icon = new ImageIcon(iconPath);
        if (icon.getIconWidth() > 0) {
            int iconSize = 50; // ขนาดรูปไอคอน
            Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        }

        // จัดรูปบน ข้อความล่าง
        btn.setVerticalAlignment(SwingConstants.BOTTOM);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        btn.setIconTextGap(5); 

        // ขอบ (ดันข้อความขึ้น 15px จากพื้น เพื่อความเท่ากัน)
        btn.setBorder(new EmptyBorder(5, 5, 15, 5)); 

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    // =================================================================
    // Class ปุ่ม Modern Shadow
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

    // =================================================================
    // Class ปุ่มย้อนกลับ (วงกลม)
    // =================================================================
    public static class CircleButton extends JButton {
        public CircleButton() {
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // วาดวงกลมส้ม
            g2.setColor(new Color(255, 140, 0));
            g2.fillOval(0, 0, getWidth(), getHeight());
            // วาดลูกศรขาว
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            int size = 8; 
            Path2D path = new Path2D.Double();
            path.moveTo(cx + size/2, cy - size); 
            path.lineTo(cx - size/2, cy);        
            path.lineTo(cx + size/2, cy + size); 
            g2.draw(path);
            g2.dispose();
        }
    }

    // =================================================================
    // Class พื้นหลัง
    // =================================================================
    public static class BackgroundPanel extends JPanel {
        private ImageIcon backgroundImageIcon;
        public BackgroundPanel() {
            backgroundImageIcon = new ImageIcon("images/พื้นหลัง.png");
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
            new StartBooking().setVisible(true);
        });
    }
}