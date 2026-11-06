package main.ui.user;

import java.awt.*;
import java.awt.geom.Path2D; // ต้อง import เพิ่ม
import javax.swing.*;

public class RoomPlan5 extends JFrame {

    public RoomPlan5() {
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("แผนผังห้อง 5 คน");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ปุ่มกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new RoomPlanMenu().setVisible(true);
            dispose();
        });

        // โลโก้
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

        // หัวข้อ
        JLabel title = new JLabel("แผนผังห้อง 5 คน");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setBounds(240, 50, 600, 40);
        bg.add(title);

        // รูปผังห้อง
        ImageIcon planIcon = new ImageIcon("images/แผนผัง5.png");
        if (planIcon.getIconWidth() > 0) {
             Image img = planIcon.getImage().getScaledInstance(600, 350, Image.SCALE_SMOOTH);
             planIcon = new ImageIcon(img);
        }
        JLabel image = new JLabel(planIcon);
        image.setBounds(210, 120, 600, 350);
        bg.add(image);
    }

    // ==========================================
    // เพิ่ม Class ย่อย (ต้องมีในทุกไฟล์ถ้าไม่ได้แยกไฟล์ Class ออกไปต่างหาก)
    // ==========================================

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
            g2.setColor(new Color(255, 140, 0));
            g2.fillOval(0, 0, getWidth(), getHeight());
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
}