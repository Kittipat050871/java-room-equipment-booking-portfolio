package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import java.io.File; // [เพิ่ม]
import java.sql.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter; // [เพิ่ม]

import main.MainMenu.BackgroundPanel;
import main.service.AuthService; // [เพิ่ม]
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton; 

public class UserStatusUI extends JFrame {

    private JPanel listContainer;
    private JTextField txtSearch;
    private Timer refreshTimer; 
    private String adminUser;
    private AuthService authService; // [เพิ่ม]

    public UserStatusUI(String username) {
        this.adminUser = username;
        this.authService = new AuthService(); // [เพิ่ม]
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("สถานะผู้ใช้ (Real-time)");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout()); // เปลี่ยน Layout เป็น BorderLayout
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(1024, 100));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // 1. ฝั่งซ้าย: ปุ่ม Back + Logo + Title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftHeader.setOpaque(false);

        CircleButton btnBack = new CircleButton();
        btnBack.setPreferredSize(new Dimension(50, 50));
        btnBack.addActionListener(e -> {
            stopTimer();
            new AdminMainMenu(adminUser).setVisible(true); 
            dispose();
        });
        leftHeader.add(btnBack);

        // Logo
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 100;
        int newHeight = (int) ((double) logoIcon.getIconHeight() / logoIcon.getIconWidth() * newWidth);
        JLabel logo = new JLabel(new ImageIcon(logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)));
        leftHeader.add(logo);

        JLabel title = new JLabel(" สถานะผู้ใช้");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 36));
        leftHeader.add(title);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);

        // 2. ฝั่งขวา: ปุ่ม Import CSV [เพิ่มส่วนนี้]
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        rightHeader.setOpaque(false);
        
        JButton btnImport = new JButton("📥 นำเข้า CSV");
        btnImport.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        btnImport.setBackground(new Color(22, 163, 74)); // สีเขียว
        btnImport.setForeground(Color.BLACK);
        btnImport.setFocusPainted(false);
        btnImport.setPreferredSize(new Dimension(140, 40));
        btnImport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("เลือกไฟล์รายชื่อ (.csv)");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // เรียกใช้ Service
                String result = authService.importUsersFromCSV(file);
                JOptionPane.showMessageDialog(this, result, "ผลการนำเข้า", JOptionPane.INFORMATION_MESSAGE);
                loadUsers(""); // รีโหลดข้อมูลทันที
            }
        });
        
        rightHeader.add(btnImport);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        bg.add(headerPanel, BorderLayout.NORTH);

        // --- Main Content ---
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 50, 30, 50));

        // Search Bar Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadUsers(txtSearch.getText());
            }
        });
        searchPanel.add(new JLabel("🔍 ค้นหา (ชื่อ/รหัส): "));
        searchPanel.add(txtSearch);

        // Table Header
        JPanel tableHeader = new JPanel(new GridLayout(1, 5));
        tableHeader.setBackground(new Color(230, 230, 230));
        tableHeader.setPreferredSize(new Dimension(800, 50));
        String[] headers = {"รหัสนักศึกษา", "ชื่อ-นามสกุล", "บทบาท", "สถานะ", "การจัดการ"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
            tableHeader.add(lbl);
        }

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(tableHeader, BorderLayout.NORTH);
        tableWrapper.add(scroll, BorderLayout.CENTER);

        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tableWrapper, BorderLayout.CENTER);
        bg.add(contentPanel, BorderLayout.CENTER);

        // โหลดครั้งแรก
        loadUsers(""); 

        refreshTimer = new Timer(5000, e -> {
            if (!txtSearch.isFocusOwner() || txtSearch.getText().isEmpty()) {
                loadUsers(txtSearch.getText());
            }
        });
        refreshTimer.start();
    }

    private void stopTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }

    private void loadUsers(String keyword) {
        SwingWorker<Void, JPanel> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String sql = "SELECT * FROM users WHERE username LIKE ? OR name LIKE ? ORDER BY role ASC, username ASC";
                try (Connection conn = DatabaseConnector.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setString(1, "%" + keyword + "%");
                    pstmt.setString(2, "%" + keyword + "%");
                    ResultSet rs = pstmt.executeQuery();

                    SwingUtilities.invokeLater(() -> listContainer.removeAll());

                    while (rs.next()) {
                        String user = rs.getString("username");
                        String name = rs.getString("name");
                        if (name == null || name.isEmpty()) name = "-";
                        String role = rs.getString("role");
                        String status = rs.getString("status");
                        if (status == null) status = "Offline";

                        JPanel row = createRow(user, name, role, status);
                        SwingUtilities.invokeLater(() -> {
                            listContainer.add(row);
                            listContainer.revalidate();
                        });
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                listContainer.repaint();
            }
        };
        worker.execute();
    }

    private JPanel createRow(String user, String name, String role, String status) {
        JPanel row = new JPanel(new GridLayout(1, 5));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            new EmptyBorder(10, 10, 10, 10)
        ));
        row.setMaximumSize(new Dimension(2000, 60));

        row.add(new JLabel(user, SwingConstants.CENTER));
        
        JLabel lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setForeground(new Color(0, 0, 139));
        row.add(lblName);

        row.add(new JLabel(role.equals("Admin") ? "ผู้ดูแลระบบ" : "ผู้ใช้", SwingConstants.CENTER));
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.setOpaque(false);
        statusPanel.add(new StatusBadge(status));
        row.add(statusPanel);

        JButton btnDetail = new JButton("ดูรายละเอียด");
        btnDetail.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        btnDetail.setBackground(Color.WHITE);
        btnDetail.setForeground(new Color(102, 0, 102));
        btnDetail.setBorder(BorderFactory.createLineBorder(new Color(102, 0, 102), 1));
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.setPreferredSize(new Dimension(110, 30));
        
        btnDetail.addActionListener(e -> {
            stopTimer();
            new UserDetailsUI(user).setVisible(true);
            dispose();
        });
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnDetail);
        row.add(btnWrapper);

        return row;
    }

    class StatusBadge extends JLabel {
        private Color bgColor;
        public StatusBadge(String status) {
            setHorizontalAlignment(CENTER);
            setFont(new Font("Leelawadee UI", Font.BOLD, 14));
            setPreferredSize(new Dimension(140, 30)); 
            
            if (status.equalsIgnoreCase("Active") || status.equals("ใช้งานอยู่")) {
                setText("ใช้งานอยู่");
                bgColor = new Color(220, 255, 220); 
                setForeground(new Color(0, 100, 0));   
            } 
            else if (status.equalsIgnoreCase("Disabled") || status.equalsIgnoreCase("Inactive")) {
                setText("ไม่ได้ใช้งาน"); 
                bgColor = new Color(220, 220, 220); 
                setForeground(new Color(80, 80, 80)); 
            } 
            else if (status.equalsIgnoreCase("Pending")) {
                setText("รออนุมัติ");
                bgColor = new Color(255, 245, 200); 
                setForeground(new Color(150, 100, 0));
            } 
            else {
                setText("ไม่ได้ใช้งาน");
                bgColor = new Color(255, 220, 220); 
                setForeground(new Color(150, 0, 0));
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserStatusUI("admin").setVisible(true)); 
    }
}