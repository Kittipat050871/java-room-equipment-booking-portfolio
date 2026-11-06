package main.ui.admin;

import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import main.MainMenu.BackgroundPanel;
import main.service.DatabaseConnector;
import main.service.DatabaseHelper;
import main.ui.user.StartBooking.CircleButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EquipStatusFrame extends JFrame {

    private JPanel cameraPanel, ipadHighPanel, ipadStdPanel; 
    private Map<Integer, String> pendingChanges = new HashMap<>();
    private JTextField txtSearch;
    private final String currentLoggedInUser = "admin"; 
    private String adminUser;
    
    public EquipStatusFrame(String username) {
        this.adminUser = username; // เก็บชื่อไว้ใช้งาน
        ensureEquipmentStatusColumn();

        try {
            ImageIcon appIcon = new ImageIcon("images/logo.png");
            this.setIconImage(appIcon.getImage());
        } catch (Exception e) {}

        setTitle("จัดการสถานะอุปกรณ์");
        setSize(1550, 850); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout()); 
        setContentPane(bg);

        // =========================================================
        // Header Section (ปรับปรุงใหม่: เพิ่มโลโก้และจัดตำแหน่ง)
        // =========================================================
        JPanel headerPanel = new JPanel(null);
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1450, 130)); // เพิ่มความสูงเล็กน้อยเพื่อให้ดูโปร่ง

        // 1. ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 25, 50, 50);
        btnBack.addActionListener(e -> { 
            new ManageBookingMenu(adminUser).setVisible(true); 
            dispose(); 
        });
        headerPanel.add(btnBack);

        // 2. โลโก้ SUT (ส่วนที่เพิ่มใหม่)
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth() > 0 ? logoIcon.getIconWidth() : 1;
        int newHeight = (int) ((double) logoIcon.getIconHeight() / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(90, 10, newWidth, newHeight); 
        headerPanel.add(logo);

        // 3. หัวข้อ Title (ขยับตำแหน่ง x เพื่อไม่ให้ทับโลโก้)
        JLabel title = new JLabel("ปรับเปลี่ยนสถานะอุปกรณ์");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 36));
        title.setBounds(250, 30, 500, 45);
        headerPanel.add(title);

        // 4. ส่วนค้นหา (Search Bar) ขยับตำแหน่ง x ตามพาดหัว
        JLabel lblSearch = new JLabel("ค้นหาอุปกรณ์:");
        lblSearch.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        lblSearch.setBounds(250, 85, 100, 30);
        headerPanel.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(360, 85, 350, 30);
        txtSearch.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        headerPanel.add(txtSearch);

        // 5. ปุ่มเพิ่มและบันทึก
        RoundedButton btnAddNew = new RoundedButton("+ เพิ่มอุปกรณ์ใหม่");
        btnAddNew.setBackground(new Color(34, 197, 94));
        btnAddNew.setForeground(Color.WHITE);
        btnAddNew.setBounds(900, 35, 200, 45);
        btnAddNew.addActionListener(e -> {
            new AddEquipDialog(this, new DatabaseHelper(), currentLoggedInUser).setVisible(true);  
            loadData(); 
        });
        headerPanel.add(btnAddNew);

        RoundedButton btnSaveAll = new RoundedButton("บันทึกทั้งหมด"); 
        btnSaveAll.setBackground(new Color(37, 99, 235));
        btnSaveAll.setForeground(Color.WHITE);
        btnSaveAll.setBounds(1120, 35, 230, 45);
        btnSaveAll.addActionListener(e -> saveAllChanges());
        headerPanel.add(btnSaveAll);

        // บังคับลำดับเลเยอร์ให้ปุ่ม Back อยู่หน้าสุด
        headerPanel.setComponentZOrder(btnBack, 0);

        bg.add(headerPanel, BorderLayout.NORTH); 

        // =========================================================
        // Content Section - 3 คอลัมน์ (โค้ดเดิมของคุณ)
        // =========================================================
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 25, 0)); 
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(10, 30, 30, 30)); 

        contentPanel.add(createColumnPanel("Camera & Accessories", 1)); 
        contentPanel.add(createColumnPanel("iPad High-End (Pro/Air)", 2));               
        contentPanel.add(createColumnPanel("Other / iPad Standard", 3));            

        bg.add(contentPanel, BorderLayout.CENTER); 
        loadData();
    }

    private JPanel createColumnPanel(String titleText, int type) {
        JPanel colPanel = new JPanel(new BorderLayout());
        colPanel.setOpaque(false);
        colPanel.setPreferredSize(new Dimension(480, 600));

        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 22));
        colPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); 
        listPanel.setOpaque(false); 
        
        if (type == 1) cameraPanel = listPanel;
        else if (type == 2) ipadHighPanel = listPanel;
        else if (type == 3) ipadStdPanel = listPanel;

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        
        colPanel.add(scroll, BorderLayout.CENTER);
        return colPanel;
    }

    private void loadData() {
        cameraPanel.removeAll(); 
        ipadHighPanel.removeAll(); 
        ipadStdPanel.removeAll();
        pendingChanges.clear();

        String searchText = txtSearch.getText().trim().toLowerCase();
        String sql = "SELECT * FROM equipment WHERE equipment_name LIKE ? ORDER BY equipment_name";
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("equipment_id");
                String rawName = rs.getString("equipment_name");
                String nameUpper = rawName.toUpperCase();
                String status = rs.getString("status") == null ? "Ready" : rs.getString("status");

                JPanel row = createListRow(id, rawName, status);

                if (nameUpper.contains("CANON") || nameUpper.contains("SONY") || 
                    nameUpper.contains("CAMERA") || nameUpper.contains("STAND") || 
                    nameUpper.contains("MIC") || nameUpper.contains("ขาตั้งกล้อง") ||
                	nameUpper.contains("เลนส์") || nameUpper.contains("ไมค์") ||
            	nameUpper.contains("ชุดไฟ")) 
                {
                    cameraPanel.add(row);
                    cameraPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                } 
                else if (nameUpper.contains("PRO") || nameUpper.contains("AIR")) {
                    ipadHighPanel.add(row);
                    ipadHighPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                } 
                else {
                    ipadStdPanel.add(row);
                    ipadStdPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            revalidate(); repaint();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private JPanel createListRow(int id, String name, String currentStatus) {
        JPanel row = new JPanel(null);
        row.setPreferredSize(new Dimension(420, 70)); 
        row.setMinimumSize(new Dimension(420, 70));
        row.setMaximumSize(new Dimension(420, 70)); 
        row.setBackground(new Color(255, 255, 255, 230));
        row.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        lblName.setBounds(15, 20, 130, 30);
        lblName.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblName.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String newName = JOptionPane.showInputDialog(EquipStatusFrame.this, "แก้ไขชื่ออุปกรณ์:", name);
                if (newName != null && !newName.trim().isEmpty()) updateEquipmentName(id, newName.trim());
            }
        });
        row.add(lblName);

        StatusLabel lblStatus = new StatusLabel(currentStatus);
        lblStatus.setBounds(150, 22, 90, 26); 
        row.add(lblStatus);

        JButton btnDelete = new JButton("ลบ"); 
        btnDelete.setBounds(250, 20, 55, 30); 
        btnDelete.setBackground(Color.WHITE);
        btnDelete.setForeground(new Color(239, 68, 68)); 
        btnDelete.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        btnDelete.setBorder(BorderFactory.createLineBorder(new Color(239, 68, 68), 1));
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "ลบ " + name + "?", "ยืนยันการลบ", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) deleteEquipment(id);
        });
        row.add(btnDelete);

        JButton btnEditStatus = new JButton("✎"); 
        btnEditStatus.setBounds(315, 20, 45, 30); 
        btnEditStatus.addActionListener(e -> {
            String next = currentStatus.equals("Ready") ? "NotReady" : 
                         currentStatus.equals("NotReady") ? "Broken" : "Ready";
            pendingChanges.put(id, next);
            saveAllChanges(); 
        });
        row.add(btnEditStatus);

        return row;
    }

    private void deleteEquipment(int id) {
        if (DatabaseHelper.deleteEquipmentFromDB(id)) {
            JOptionPane.showMessageDialog(this, "ลบอุปกรณ์เรียบร้อยแล้ว");
            loadData();
        }
    }

    private void saveAllChanges() {
        if (pendingChanges.isEmpty()) return;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE equipment SET status = ? WHERE equipment_id = ?")) {
            conn.setAutoCommit(false);
            for (Map.Entry<Integer, String> entry : pendingChanges.entrySet()) {
                pstmt.setString(1, entry.getValue());
                pstmt.setInt(2, entry.getKey());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateEquipmentName(int id, String newName) {
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE equipment SET equipment_name = ? WHERE equipment_id = ?")) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void ensureEquipmentStatusColumn() {
        try (Connection conn = DatabaseConnector.connect(); Statement stmt = conn.createStatement()) {
            try { stmt.executeQuery("SELECT status FROM equipment LIMIT 1"); } 
            catch (SQLException e) { stmt.executeUpdate("ALTER TABLE equipment ADD COLUMN status TEXT DEFAULT 'Ready'"); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new EquipStatusFrame("admin").setVisible(true)); }

    class StatusLabel extends JLabel {
        private String statusKey;
        public StatusLabel(String statusKey) {
            setStatus(statusKey);
            setHorizontalAlignment(CENTER);
            setForeground(Color.WHITE);
            setFont(new Font("Leelawadee UI", Font.BOLD, 12));
        }
        public void setStatus(String statusKey) {
            this.statusKey = statusKey;
            switch (statusKey) {
                case "Ready": setText("พร้อมใช้งาน"); break;
                case "NotReady": setText("ไม่พร้อม"); break;
                case "Broken": setText("ชำรุด"); break;
                default: setText(statusKey);
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if ("Ready".equals(statusKey)) g2.setColor(new Color(34, 197, 94)); 
            else if ("NotReady".equals(statusKey)) g2.setColor(new Color(239, 68, 68)); 
            else if ("Broken".equals(statusKey)) g2.setColor(new Color(255, 165, 0)); 
            else g2.setColor(Color.GRAY);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
        }
    }

    class RoundedButton extends JButton {
        private int radius = 30; 
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);     
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isArmed() ? getBackground().darker() : getBackground()); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }
}