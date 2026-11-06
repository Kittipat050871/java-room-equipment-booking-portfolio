package main.ui.user;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.ui.admin.AdminMainMenu.ModernShadowButton;
import main.ui.user.RoomPlan5.CircleButton;

public class RoomPlanMenu extends JFrame {

    public RoomPlanMenu() {
    	
    	ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("แผนผังห้อง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        RoomPlanBackground bg = new RoomPlanBackground();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== ปุ่มย้อนกลับ (วงกลมสีส้ม) =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new StartBooking().setVisible(true);
            dispose();
        });

     // ----- โลโก้ -----
        ImageIcon logoIcon = new ImageIcon("images/logo.png"); // 1. โหลดตัวเต็ม
        
        // 2. "กำหนด" ความกว้าง (Width) ที่คุณอยากได้
        // (ดูจากรูป... 180 น่าจะสวย... ถ้า "เล็ก" ไป... ก็ "เพิ่ม" เลขนี้ครับ)
        int newWidth = 150; 

        // 3. "คำนวณ" ความสูง (Height) อัตโนมัติ ... (นี่คือ "เคล็ดลับ" ครับ!)
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);

        // 4. "สั่งย่อ" (Scale) ... โดยใช้ "ขนาดใหม่" ที่ "ไม่" เพี้ยน
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // 5. "แปะ" (ตัวที่ย่อแล้ว)
        JLabel logo = new JLabel(scaledIcon); 
        
        // 6. "ตั้งกรอบ" (Bounds) ... ให้ "เท่า" ขนาดใหม่
        logo.setBounds(70, 10, newWidth, newHeight); 
        bg.add(logo);


        // ===== หัวข้อ =====
        JLabel title = new JLabel("แผนผังห้อง");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(new Color(50, 50, 50));
        title.setBounds(230, 35, 600, 40);
        bg.add(title);

     // ===== ปุ่มเมนู (แบบใหม่: ขาว, เงา, มีรูป) =====
        Font f = new Font("Leelawadee UI", Font.BOLD, 18);

        // *** อย่าลืมเอารูปไปใส่ใน folder images นะครับ ***
        JButton b1 = createShadowButton("ห้องใหญ่สำหรับ 8คน", "images/Meeting Room.png", f);
        JButton b2 = createShadowButton("ห้องเล็กสำหรับ 5คน", "images/Meeting Room.png", f);
   

        // กำหนดขนาดปุ่ม (เอาให้เท่ากับ MainMenu ของ User)
        int w = 230, h = 120;
        int y1 = 160;
        int y2 = 300; 

        // จัดวางตำแหน่ง (จัดกึ่งกลางหน้าจอแบบ 2x2)
        // คำนวณกึ่งกลางหน้าจอ (1024/2 = 512)
        // ซ้าย = 512 - 230 - 10 = 272
        // ขวา = 512 + 10 = 522
        // (เว้นช่องไฟตรงกลาง 20px)

        int leftX = 272;
        int rightX = 522;

        b1.setBounds(leftX, y1, w, h);  // ซ้ายบน
        b2.setBounds(rightX, y1, w, h); // ขวาบน
      

        bg.add(b1);
        bg.add(b2);
       

        // ===== Action =====
        b1.addActionListener(e -> {
            new RoomPlan8().setVisible(true);
            dispose();
        });

        b2.addActionListener(e -> {
            new RoomPlan5().setVisible(true);
            dispose();
        });

       
    }

    // =================================================================
    // Method สร้างปุ่ม (เหมือนของ MainMenu เป๊ะ)
    // =================================================================
    private JButton createShadowButton(String text, String iconPath, Font font) {
        ModernShadowButton btn = new ModernShadowButton(text);
        btn.setFont(font);
        btn.setForeground(Color.BLACK); 

        // ใส่รูป
        ImageIcon icon = new ImageIcon(iconPath);
        if (icon.getIconWidth() > 0) {
            int iconSize = 60; // ขนาดรูปใหญ่สวยๆ
            Image img = icon.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));
        }

        // จัดตำแหน่ง (รูปบน ข้อความล่าง)
        btn.setVerticalAlignment(SwingConstants.BOTTOM);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);

        // ระยะห่าง
        btn.setIconTextGap(5); 

        // ขอบ (ดันข้อความขึ้น 15px จากพื้น)
        btn.setBorder(new EmptyBorder(5, 5, 15, 5)); 

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =================================================================
    // Class ปุ่มมีเงา (เหมือนของ MainMenu เป๊ะ)
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

            // วาดเงา
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);

            // วาดปุ่ม
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);

            // วาดขอบบางๆ
            g2.setColor(new Color(230, 230, 230));
            g2.drawRoundRect(0, 0, getWidth() - shadowOffset -1, getHeight() - shadowOffset -1, arcSize, arcSize);

            g2.dispose();
            super.paintComponent(g);
        }
    }


/* ---------- พื้นหลังแผนผังห้อง ---------- */
class RoomPlanBackground extends JPanel {

    private Image skyline;

    public RoomPlanBackground() {
        skyline = new ImageIcon("images/พื้นหลัง.png").getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(240, 240, 240),
                0, getHeight(), new Color(200, 200, 200));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (skyline != null) {
            double scale = (double) getWidth() / skyline.getWidth(null);
            int newH = (int) (skyline.getHeight(null) * scale);

            g2.drawImage(skyline, 0, getHeight() - newH, getWidth(), newH, this);
        }
    }

}}