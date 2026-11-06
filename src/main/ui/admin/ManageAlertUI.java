package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu.BackgroundPanel;
import main.service.AuthService; 
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton; 

public class ManageAlertUI extends JFrame {

    private JPanel cardPanel;
    private CardLayout cardLayout;
    
    // Panels
    private JPanel viewPanel;
    private JPanel addPanel;
    private JPanel deletePanel;

    // Component หน้า Add
    private JSpinner spinDate;
    private JTextArea txtMessage;
    
    // [เพิ่มใหม่] ส่วนเลือกผู้รับ (Target User)
    private JRadioButton rbAll;
    private JRadioButton rbSpecific;
    private JTextField txtTargetUser;
    private JButton btnCheckUser;
    private JLabel lblCheckResult;

    private String adminUser;
    private AuthService authService; 

    public ManageAlertUI(String username) {
        this.adminUser = username;
        this.authService = new AuthService(); 
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font thaiFont = new Font("Leelawadee UI", Font.PLAIN, 14);
            UIManager.put("OptionPane.messageFont", thaiFont);
            UIManager.put("OptionPane.buttonFont", thaiFont);
        } catch (Exception e) { e.printStackTrace(); }
        
        initDatabase(); // [แก้ไข] เรียกเพื่อตรวจสอบและซ่อมตารางก่อนใช้งาน

        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("จัดการการแจ้งเตือน");
        setSize(1024, 650); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // --- Header ---
        JPanel headerPanel = new JPanel(null);
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1024, 100));

        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 25, 50, 50);
        btnBack.addActionListener(e -> {
            new AdminMainMenu(adminUser).setVisible(true);
            dispose();
        });
        headerPanel.add(btnBack);

        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth();
        int newHeight = (int) ((double) logoIcon.getIconHeight() / (originalWidth > 0 ? originalWidth : 1) * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(85, 10, newWidth, newHeight);
        headerPanel.add(logo);

        JLabel title = new JLabel("จัดการการแจ้งเตือน");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 36));
        title.setForeground(Color.BLACK);
        title.setBounds(250, 30, 500, 40);
        headerPanel.add(title);

        bg.add(headerPanel, BorderLayout.NORTH);

        // --- Content ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        
        createViewPanel();   
        createAddPanel();    
        createDeletePanel(); 

        cardPanel.add(viewPanel, "VIEW");
        cardPanel.add(addPanel, "ADD");
        cardPanel.add(deletePanel, "DELETE");

        bg.add(cardPanel, BorderLayout.CENTER);

        showViewMode(); // โหลดข้อมูลเริ่มต้น
    }

    private void createViewPanel() {
        viewPanel = new JPanel(null);
        viewPanel.setOpaque(false);

        JPanel whiteBox = new JPanel(new BorderLayout());
        whiteBox.setBackground(new Color(255, 255, 255, 240)); 
        whiteBox.setBounds(100, 20, 824, 400);
        whiteBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topBar.setOpaque(false);
        
        ModernColoredButton btnGoAdd = new ModernColoredButton("+ คลิกเพื่อเพิ่มแจ้งเตือน");
        styleMiniButton(btnGoAdd);
        
        // [แก้ไข] เมื่อกดเพิ่ม ให้ล้างค่าเก่าออกก่อน
        btnGoAdd.addActionListener(e -> {
            txtMessage.setText("");
            txtTargetUser.setText("");
            lblCheckResult.setText("");
            rbAll.setSelected(true);
            txtTargetUser.setEnabled(false);
            btnCheckUser.setEnabled(false);
            cardLayout.show(cardPanel, "ADD");
        });

        ModernColoredButton btnGoDelete = new ModernColoredButton("- คลิกเพื่อลบแจ้งเตือน");
        styleMiniButton(btnGoDelete);
        btnGoDelete.addActionListener(e -> showDeleteMode());

        topBar.add(btnGoAdd);
        topBar.add(btnGoDelete);
        whiteBox.add(topBar, BorderLayout.NORTH);

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE); 
        
        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null); 
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        whiteBox.add(scroll, BorderLayout.CENTER);
        viewPanel.add(whiteBox);
        viewPanel.putClientProperty("container", listContainer);
    }
    
    private void createDeletePanel() {
        deletePanel = new JPanel(null);
        deletePanel.setOpaque(false);
        JPanel whiteBox = new JPanel(new BorderLayout());
        whiteBox.setBackground(new Color(255, 255, 255, 240));
        whiteBox.setBounds(100, 20, 824, 400);
        whiteBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        ModernColoredButton btnDone = new ModernColoredButton("เสร็จสิ้น");
        styleMiniButton(btnDone);
        btnDone.addActionListener(e -> showViewMode());
        topBar.add(btnDone);
        whiteBox.add(topBar, BorderLayout.NORTH);
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        whiteBox.add(scroll, BorderLayout.CENTER);
        deletePanel.add(whiteBox);
        deletePanel.putClientProperty("container", listContainer);
    }

    private void createAddPanel() {
        addPanel = new JPanel(null);
        addPanel.setOpaque(false);

        JPanel whiteBox = new JPanel(null);
        whiteBox.setBackground(new Color(255, 255, 255, 240));
        whiteBox.setBounds(100, 20, 824, 450); 

        ModernColoredButton btnBackMini = new ModernColoredButton("< กลับ");
        styleMiniButton(btnBackMini);
        btnBackMini.setBounds(20, 10, 80, 25);
        btnBackMini.addActionListener(e -> showViewMode());
        whiteBox.add(btnBackMini);

        // 1. วันที่
        JLabel lblHead = new JLabel("เลือกวันที่");
        lblHead.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        lblHead.setForeground(new Color(102, 0, 102)); 
        lblHead.setBounds(50, 50, 200, 30);
        whiteBox.add(lblHead);

        spinDate = new JSpinner(new SpinnerDateModel());
        spinDate.setEditor(new JSpinner.DateEditor(spinDate, "dd/MM/yyyy"));
        spinDate.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        spinDate.setBounds(50, 80, 200, 35);
        whiteBox.add(spinDate);

        // 2. [เพิ่มใหม่] เลือกผู้รับ
        JLabel lblTarget = new JLabel("ส่งถึง");
        lblTarget.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        lblTarget.setForeground(new Color(102, 0, 102));
        lblTarget.setBounds(300, 50, 200, 30);
        whiteBox.add(lblTarget);

        rbAll = new JRadioButton("ทุกคน (Broadcast)");
        rbAll.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        rbAll.setOpaque(false);
        rbAll.setBounds(300, 80, 150, 30);
        rbAll.setSelected(true); 

        rbSpecific = new JRadioButton("ระบุผู้ใช้ (Private)");
        rbSpecific.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        rbSpecific.setOpaque(false);
        rbSpecific.setBounds(460, 80, 150, 30);

        ButtonGroup grp = new ButtonGroup();
        grp.add(rbAll);
        grp.add(rbSpecific);
        whiteBox.add(rbAll);
        whiteBox.add(rbSpecific);

        txtTargetUser = new JTextField();
        txtTargetUser.setBounds(460, 110, 150, 30);
        txtTargetUser.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        txtTargetUser.setEnabled(false);
        whiteBox.add(txtTargetUser);

        btnCheckUser = new JButton("ตรวจสอบ");
        btnCheckUser.setBounds(620, 110, 80, 30);
        btnCheckUser.setFont(new Font("Leelawadee UI", Font.PLAIN, 12));
        btnCheckUser.setEnabled(false);
        whiteBox.add(btnCheckUser);

        lblCheckResult = new JLabel("");
        lblCheckResult.setFont(new Font("Leelawadee UI", Font.BOLD, 12));
        lblCheckResult.setBounds(460, 140, 250, 20);
        whiteBox.add(lblCheckResult);

        // Logic ควบคุมการเปิดปิดช่องกรอกชื่อ
        ActionListener radioListener = e -> {
            boolean isSpecific = rbSpecific.isSelected();
            txtTargetUser.setEnabled(isSpecific);
            btnCheckUser.setEnabled(isSpecific);
            if (!isSpecific) {
                txtTargetUser.setText("");
                lblCheckResult.setText("");
            }
        };
        rbAll.addActionListener(radioListener);
        rbSpecific.addActionListener(radioListener);

        // ปุ่มตรวจสอบชื่อผู้ใช้
        btnCheckUser.addActionListener(e -> {
            String u = txtTargetUser.getText().trim();
            if (u.isEmpty()) return;
            if (authService.isUserExists(u)) { //แดงครับ
                lblCheckResult.setText("✓ พบผู้ใช้ " + u);
                lblCheckResult.setForeground(new Color(34, 197, 94));
            } else {
                lblCheckResult.setText("❌ ไม่พบผู้ใช้");
                lblCheckResult.setForeground(Color.RED);
            }
        });

        // 3. ข้อความ
        JLabel lblMsg = new JLabel("รายละเอียดแจ้งเตือน");
        lblMsg.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        lblMsg.setForeground(new Color(102, 0, 102));
        lblMsg.setBounds(50, 160, 300, 30);
        whiteBox.add(lblMsg);

        txtMessage = new JTextArea();
        txtMessage.setFont(new Font("Leelawadee UI", Font.PLAIN, 18));
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        
        JScrollPane scrollMsg = new JScrollPane(txtMessage);
        scrollMsg.setBounds(50, 190, 720, 150);
        whiteBox.add(scrollMsg);

        ModernColoredButton btnConfirm = new ModernColoredButton("ยืนยันการบันทึก");
        btnConfirm.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(102, 0, 153)); 
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBounds(312, 360, 200, 45); 
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> saveAlert());
        whiteBox.add(btnConfirm);

        addPanel.add(whiteBox);
    }

    private void saveAlert() {
        String msg = txtMessage.getText();
        if (msg.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกข้อความแจ้งเตือน");
            return;
        }

        String targetUser = "ALL"; 
        if (rbSpecific.isSelected()) {
            targetUser = txtTargetUser.getText().trim();
            if (targetUser.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณาระบุชื่อผู้ใช้ที่จะส่งถึง");
                return;
            }
            if (!authService.isUserExists(targetUser)) { //แดงครับ
                JOptionPane.showMessageDialog(this, "ไม่พบชื่อผู้ใช้ '" + targetUser + "' ในระบบ");
                return;
            }
        }

        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format((Date) spinDate.getValue());
        
        // ใช้ AuthService แทนการเขียน SQL สด เพื่อความปลอดภัยและเป็นระเบียบ
        boolean success = authService.createAlert(dateStr, msg, targetUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "บันทึกแจ้งเตือนเรียบร้อย (ส่งถึง: " + targetUser + ")");
            // [แก้ไข] สั่งให้โหลดหน้า View และรีเฟรชข้อมูลใหม่ทันที
            showViewMode(); 
        } else {
            JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการบันทึก");
        }
    }

    private void initDatabase() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement()) {
             
            // สร้างตาราง
            stmt.execute("CREATE TABLE IF NOT EXISTS alerts (id INTEGER PRIMARY KEY AUTOINCREMENT, alert_date TEXT, message TEXT, target_user TEXT DEFAULT 'ALL')");
            
            // [แก้ไข] พยายามเพิ่ม column ถ้ายังไม่มี (กรณีตารางเก่ามีอยู่แล้ว)
            try {
                stmt.execute("ALTER TABLE alerts ADD COLUMN target_user TEXT DEFAULT 'ALL'");
            } catch (SQLException e) {
                // ถ้ามี column อยู่แล้ว จะ error ซึ่งเราก็แค่ข้ามไป
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // =========================================================
    // [แก้ไข] Load Alerts ปรับปรุงให้ดึงข้อมูลแม่นยำและแสดงผลถูกต้อง
    // =========================================================
    private void loadAlerts(JPanel container, boolean isDeleteMode) {
        if (container == null) return;
        container.removeAll(); // ล้างของเก่า
        
        String sql = "SELECT * FROM alerts ORDER BY id DESC";
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("alert_date");
                String msg = rs.getString("message");
                
                String target = "ALL";
                try { 
                    target = rs.getString("target_user"); 
                    if (target == null) target = "ALL";
                } catch(Exception ex) { target = "ALL"; }

                JPanel row = new JPanel(new BorderLayout(15, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                    new EmptyBorder(15, 10, 15, 10)
                ));
                row.setMaximumSize(new Dimension(800, 70));

                row.add(new DateBadge(date), BorderLayout.WEST);

                String displayMsg = msg;
                if (!"ALL".equals(target)) {
                    displayMsg = "[ถึง: " + target + "] " + msg;
                }

                JTextArea txtDisplay = new JTextArea(displayMsg);
                txtDisplay.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
                
                // แยกสีข้อความให้แอดมินดูง่าย
                if (!"ALL".equals(target)) {
                    txtDisplay.setForeground(new Color(0, 102, 204)); // สีฟ้า (Private)
                } else {
                    txtDisplay.setForeground(Color.DARK_GRAY); // สีเทา (Broadcast)
                }
                
                txtDisplay.setLineWrap(true);
                txtDisplay.setWrapStyleWord(true);
                txtDisplay.setOpaque(false);
                txtDisplay.setEditable(false);
                
                row.add(txtDisplay, BorderLayout.CENTER);

                if (isDeleteMode) {
                    JButton btnDel = new JButton("🗑");
                    btnDel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                    btnDel.setForeground(Color.RED);
                    btnDel.setContentAreaFilled(false);
                    btnDel.addActionListener(e -> deleteAlert(id));
                    row.add(btnDel, BorderLayout.EAST);
                }
                container.add(row);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // [แก้ไข] บังคับวาดหน้าจอใหม่
        container.revalidate();
        container.repaint();
    }
    
    private void deleteAlert(int id) {
        if (JOptionPane.showConfirmDialog(this, "ต้องการลบรายการนี้ใช่ไหม?", "ยืนยัน", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnector.connect();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM alerts WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                showDeleteMode(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void showViewMode() {
        cardLayout.show(cardPanel, "VIEW");
        loadAlerts((JPanel) viewPanel.getClientProperty("container"), false);
    }

    private void showDeleteMode() {
        cardLayout.show(cardPanel, "DELETE");
        loadAlerts((JPanel) deletePanel.getClientProperty("container"), true);
    }
    
    private void styleMiniButton(JButton btn) {
        btn.setFont(new Font("Leelawadee UI", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);

        Color primaryColor;
        if (btn.getText().contains("+")) {
            primaryColor = new Color(0, 122, 255); 
        } else if (btn.getText().contains("-")) {
            primaryColor = new Color(175, 82, 222); 
        } else {
            primaryColor = new Color(100, 100, 100); 
        }
        btn.setBackground(primaryColor);
    }

    class DateBadge extends JLabel {
        public DateBadge(String text) {
            super(text);
            setFont(new Font("Leelawadee UI", Font.BOLD, 14));
            setForeground(new Color(80, 0, 80));
            setHorizontalAlignment(CENTER);
            setBorder(new EmptyBorder(5, 20, 5, 10)); 
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 235, 255));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(200, 50, 100)); 
            g2.fillOval(8, getHeight()/2 - 4, 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernColoredButton extends JButton {
        public ModernColoredButton(String text) {
            super(text);
            setContentAreaFilled(false); 
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageAlertUI("admin").setVisible(true));
    }
}