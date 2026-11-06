package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton;

public class StatsUI extends JFrame {
    private String adminUser;
    private Map<String, Integer> dailyStats = new LinkedHashMap<>();
    private Map<String, Integer> equipStats = new LinkedHashMap<>();
    private int totalBookings = 0;
    private int approvedCount = 0;
    private String topDay = "-";
    private String topEquip = "-";

    private final Color[] PIE_COLORS = {
        new Color(79, 70, 229),   // Indigo
        new Color(16, 185, 129),  // Emerald
        new Color(245, 158, 11),  // Amber
        new Color(239, 68, 68),   // Red
        new Color(139, 92, 246),  // Violet
        new Color(14, 165, 233)   // Sky
    };

    public StatsUI(String adminUser) {
        this.adminUser = adminUser;
        setTitle("สถิติการใช้งาน - Admin: " + adminUser);
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ใช้ BackgroundPanel
        AdminMainMenu.BackgroundPanel bg = new AdminMainMenu.BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        loadData();

        // =========================================================
        // Header Section
        // =========================================================
        JPanel headerPanel = new JPanel(null);
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1200, 100));

        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 25, 50, 50);
        btnBack.addActionListener(e -> {
            new AdminMainMenu(adminUser).setVisible(true);
            dispose();
        });
        headerPanel.add(btnBack);

        // โลโก้
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int logoW = 120;
        int logoH = (int) ((double) logoIcon.getIconHeight() / (logoIcon.getIconWidth() > 0 ? logoIcon.getIconWidth() : 1) * logoW);
        Image scaledImg = logoIcon.getImage().getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(scaledImg));
        lblLogo.setBounds(90, 10, logoW, logoH);
        headerPanel.add(lblLogo);

        JLabel lblTitle = new JLabel("Dashboard สถิติการใช้งาน");
        lblTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(30, 41, 59)); // Dark Blue-Grey
        lblTitle.setBounds(230, 25, 500, 50);
        headerPanel.add(lblTitle);

        // ปุ่มรีเฟรชสไตล์โมเดิร์น
        JButton btnRefresh = new JButton("รีเฟรชข้อมูล");
        

     // 2. ตั้งค่า Font และพิกัดตำแหน่ง (Bounds) ให้ถูกต้อง
     btnRefresh.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
     btnRefresh.setBounds(950, 32, 160, 40); // กำหนดตำแหน่ง x, y, width, height

     // 3. กำหนดสีพื้นหลังเป็น 79, 70, 229 ตามที่ต้องการ
     btnRefresh.setBackground(new Color(79, 70, 229));
     btnRefresh.setForeground(Color.WHITE);

     // 4. ตั้งค่าคุณสมบัติปุ่ม
     btnRefresh.setFocusPainted(false);
     btnRefresh.setBorderPainted(false); // ปิดขอบมาตรฐานเพื่อใช้ความโค้งจากคลาส Modern
     btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

     // 5. ใส่ Action และเพิ่มลงใน Panel
     btnRefresh.addActionListener(e -> { 
         loadData(); 
         revalidate(); 
         repaint(); 
     });
     headerPanel.add(btnRefresh);

        headerPanel.setComponentZOrder(btnBack, 0);
        bg.add(headerPanel, BorderLayout.NORTH);

        // =========================================================
        // Content Section
        // =========================================================
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 40, 40, 40));

        // Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 25, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(2000, 150));
        
        cardsGrid.add(createModernCard("ยอดจองทั้งหมด", String.valueOf(totalBookings), "รายการ", new Color(79, 70, 229)));
        cardsGrid.add(createModernCard("อนุมัติแล้ว", String.valueOf(approvedCount), "รายการ", new Color(16, 185, 129)));
        cardsGrid.add(createModernCard("ยอดจองสูงสุด", topDay, "วันที่", new Color(245, 158, 11)));
        cardsGrid.add(createModernCard("อุปกรณ์ยอดฮิต", topEquip, "ยอดนิยม", new Color(239, 68, 68)));
        
        mainContent.add(cardsGrid);
        mainContent.add(Box.createRigidArea(new Dimension(0, 30)));

        // Charts Section
        JPanel chartsGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        chartsGrid.setOpaque(false);
        chartsGrid.add(new BarChartPanel("แนวโน้มการใช้งาน (5 วันล่าสุด)", dailyStats));
        chartsGrid.add(new PieChartPanel("การกระจายตัวของอุปกรณ์", equipStats));
        mainContent.add(chartsGrid);

        JScrollPane scroll = new JScrollPane(mainContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        bg.add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        dailyStats.clear(); equipStats.clear();
        approvedCount = 0; topEquip = "-"; topDay = "-";
        try (Connection conn = DatabaseConnector.connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings");
            if (rs.next()) totalBookings = rs.getInt(1);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings WHERE status='อนุมัติแล้ว'");
            if (rs.next()) approvedCount = rs.getInt(1);

            rs = stmt.executeQuery("SELECT substr(start_time, 1, 10) as d, COUNT(*) as c FROM bookings GROUP BY d ORDER BY d DESC LIMIT 5");
            int maxC = -1;
            while (rs.next()) {
                String d = rs.getString("d");
                int c = rs.getInt("c");
                dailyStats.put(d, c);
                if (c > maxC) { maxC = c; topDay = d; }
            }

            rs = stmt.executeQuery("SELECT equipment_name, SUM(quantity) as q FROM booking_equipment GROUP BY equipment_name ORDER BY q DESC LIMIT 5");
            while (rs.next()) {
                String name = rs.getString("equipment_name");
                int q = rs.getInt("q");
                equipStats.put(name, q);
                if (topEquip.equals("-")) topEquip = name;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createModernCard(String title, String value, String unit, Color themeColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                // เส้นสีธีมด้านล่าง
                g2.setColor(themeColor);
                g2.fillRoundRect(0, getHeight()-6, getWidth(), 6, 0, 0);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Leelawadee UI", Font.BOLD, 26));
        lblValue.setForeground(new Color(30, 41, 59));

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Leelawadee UI", Font.PLAIN, 12));
        lblUnit.setForeground(themeColor);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(lblValue, BorderLayout.CENTER);
        centerPanel.add(lblUnit, BorderLayout.SOUTH);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);

        return card;
    }

    // --- กราฟแท่ง (BarChart) ---
    class BarChartPanel extends JPanel {
        private String title; private Map<String, Integer> data;
        BarChartPanel(String t, Map<String, Integer> d) { this.title = t; this.data = d; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            
            // Title
            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
            g2.drawString(title, 30, 45);

            if (data.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("ไม่มีข้อมูลที่จะแสดง", getWidth()/2-60, getHeight()/2);
                g2.dispose();
                return;
            }

            int max = Collections.max(data.values()); if (max <= 0) max = 1;
            int x = 80;
            int chartHeight = getHeight() - 150;
            int barWidth = 40;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int val = entry.getValue();
                int barH = (int)((double)val / max * chartHeight);
                
                // วาดแท่งกราฟ
                g2.setColor(new Color(79, 70, 229, 220));
                g2.fillRoundRect(x, getHeight() - barH - 70, barWidth, barH, 10, 10);
                
                // วาดตัวเลขบนแท่ง
                g2.setColor(new Color(30, 41, 59));
                g2.setFont(new Font("Leelawadee UI", Font.BOLD, 12));
                g2.drawString(String.valueOf(val), x + (barWidth/2) - 5, getHeight() - barH - 80);
                
                // วาดวันที่ (Label)
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("Leelawadee UI", Font.PLAIN, 11));
                String dateLabel = entry.getKey().substring(5); // เอาแค่ MM-DD
                g2.drawString(dateLabel, x, getHeight() - 40);
                
                x += 90;
            }
            g2.dispose();
        }
    }

    // --- กราฟวงกลม (PieChart) ---
    class PieChartPanel extends JPanel {
        private String title; private Map<String, Integer> data;
        PieChartPanel(String t, Map<String, Integer> d) { this.title = t; this.data = d; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            
            g2.setColor(new Color(30, 41, 59));
            g2.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
            g2.drawString(title, 30, 45);

            if (data.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("ไม่มีข้อมูลอุปกรณ์", getWidth()/2-60, getHeight()/2);
                g2.dispose();
                return;
            }

            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            int startAngle = 90;
            int i = 0;
            int legendX = 350;
            int legendY = 80;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int val = entry.getValue();
                int angle = (int)((double)val / total * 360);
                
                // Draw Slice
                g2.setColor(PIE_COLORS[i % PIE_COLORS.length]);
                g2.fillArc(50, 80, 220, 220, startAngle, angle);
                
                // Draw Legend
                g2.fillRoundRect(legendX, legendY, 15, 15, 5, 5);
                g2.setColor(new Color(30, 41, 59));
                g2.setFont(new Font("Leelawadee UI", Font.PLAIN, 13));
                g2.drawString(entry.getKey() + " (" + val + ")", legendX + 25, legendY + 13);
                
                startAngle += angle;
                legendY += 35;
                i++;
            }
            g2.dispose();
        }
    }
}