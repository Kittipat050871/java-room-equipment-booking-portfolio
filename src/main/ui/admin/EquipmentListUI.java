package main.ui.admin;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel; // [เพิ่ม] สำหรับจัดการคอลัมน์

import main.MainMenu.BackgroundPanel;
import main.service.AuthService;
import main.ui.user.StartBooking.CircleButton;

public class EquipmentListUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private List<String> originalData; // ข้อมูลต้นฉบับ
    private List<String> currentData;  // ข้อมูลที่กำลังแสดงผล
    private AuthService authService;
    
    // สีธีมหลัก (SUT Orange)
    private final Color THEME_ORANGE = new Color(242, 101, 34);
    private final Color THEME_BG_GRAY = new Color(248, 248, 248);

    public EquipmentListUI() {
        authService = new AuthService();
        
        // Setup หน้าต่าง
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());
        setTitle("รายชื่ออุปกรณ์ (Sorting & Searching)");
        setSize(900, 700); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // --- 1. Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setPreferredSize(new Dimension(900, 80));
        headerPanel.setBorder(new EmptyBorder(15, 20, 10, 20));

        // ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setPreferredSize(new Dimension(50, 50));
        btnBack.addActionListener(e -> dispose());
        
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(btnBack);
        
        // หัวข้อหน้า
        JLabel title = new JLabel(" รายการอุปกรณ์");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(Color.BLACK); 
        leftHeader.add(title);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        bg.add(headerPanel, BorderLayout.NORTH);

        // --- 2. Tool Bar (Search & Sort) ---
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolBar.setOpaque(false);
        toolBar.setBorder(new EmptyBorder(0, 20, 10, 20));

        // ช่องค้นหา (Data Searching)
        JLabel lblSearch = new JLabel("🔍 ค้นหา:");
        lblSearch.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        toolBar.add(lblSearch);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        // แต่งขอบช่องค้นหา
        txtSearch.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true), 
                new EmptyBorder(5, 10, 5, 10)));
        
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch(txtSearch.getText());
            }
        });
        toolBar.add(txtSearch);

        // ปุ่มเรียงลำดับ (Data Sorting)
        toolBar.add(Box.createHorizontalStrut(20)); 
        
        JButton btnSortAZ = new JButton("เรียง ก-ฮ (A-Z)");
        JButton btnSortZA = new JButton("เรียง ฮ-ก (Z-A)");
        
        styleModernButton(btnSortAZ);
        styleModernButton(btnSortZA);

        btnSortAZ.addActionListener(e -> performSort(true));  // true = Ascending
        btnSortZA.addActionListener(e -> performSort(false)); // false = Descending

        toolBar.add(btnSortAZ);
        toolBar.add(btnSortZA);

        // --- 3. Table Section ---
        String[] cols = {"ลำดับ", "ชื่ออุปกรณ์"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table); 

        // จัด Layout ตรงกลาง
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(toolBar, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(230, 230, 230), 1)); 
        scrollPane.getViewport().setBackground(Color.WHITE); 
        
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(0, 30, 30, 30));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        centerPanel.add(tableContainer, BorderLayout.CENTER);

        bg.add(centerPanel, BorderLayout.CENTER);

        // --- โหลดข้อมูล ---
        loadDataFromDB();
    }

    // ==========================================================
    // UI Styling Methods
    // ==========================================================
    private final Color SKY_BLUE = new Color(135, 206, 235);

    private void styleModernButton(JButton btn) {
        btn.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);          // พื้นหลังปกติเป็นสีขาว
        btn.setForeground(SKY_BLUE);             // ตัวหนังสือปกติเป็นฟ้าอ่อน (Sky Blue)
        btn.setBorder(new LineBorder(SKY_BLUE, 1, true)); // ขอบสีฟ้าอ่อน
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 35));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // เมื่อเมาส์ชี้:
                btn.setBackground(new Color(245, 250, 255)); // พื้นหลังเปลี่ยนเป็นฟ้าจางมากๆ
                btn.setForeground(Color.blue);               // ตัวหนังสือกลายเป็นสีน้ำเงินที่คุณต้องการ
                btn.setBorder(new LineBorder(Color.blue, 1, true)); // ขอบเปลี่ยนเป็นสีน้ำเงินให้เข้ากัน
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // เมื่อเมาส์ออก: กลับสู่ค่าปกติ
                btn.setBackground(Color.WHITE);
                btn.setForeground(SKY_BLUE);
                btn.setBorder(new LineBorder(SKY_BLUE, 1, true));
            }
        });
    }

    private void styleTable(JTable table) {
        table.setRowHeight(40); 
        table.setFont(new Font("Leelawadee UI", Font.PLAIN, 15));
        table.setShowVerticalLines(false); 
        table.setGridColor(new Color(230, 230, 230)); 
        table.setIntercellSpacing(new Dimension(0, 0)); 
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Leelawadee UI", Font.BOLD, 16));
        header.setBackground(new Color(245, 245, 245)); 
        header.setForeground(new Color(60, 60, 60)); 
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);  
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(600); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 0) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT); 
                    setBorder(new EmptyBorder(0, 15, 0, 0)); 
                }

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(THEME_BG_GRAY); 
                    }
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(255, 229, 204)); 
                    c.setForeground(Color.BLACK); 
                }
                
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }

    // ==========================================================
    // Logic Methods
    // ==========================================================

    private void loadDataFromDB() {
        originalData = authService.getAllEquipmentNames();
        currentData = new ArrayList<>(originalData);
        updateTable();
    }

    private void updateTable() {
        model.setRowCount(0);
        for (int i = 0; i < currentData.size(); i++) {
            model.addRow(new Object[]{ (i+1), currentData.get(i) });
        }
    }

    // Data Searching (Linear Search)
    private void performSearch(String keyword) {
        currentData.clear();
        for (String item : originalData) {
            if (item.toLowerCase().contains(keyword.toLowerCase())) {
                currentData.add(item);
            }
        }
        updateTable();
    }

    // Data Sorting (Bubble Sort) + Visual Indicator
    private void performSort(boolean ascending) {
        int n = currentData.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                String s1 = currentData.get(j);
                String s2 = currentData.get(j + 1);
                int compareResult = s1.compareToIgnoreCase(s2);
                
                boolean swapNeeded = false;
                if (ascending) {
                    if (compareResult > 0) swapNeeded = true;
                } else {
                    if (compareResult < 0) swapNeeded = true;
                }

                if (swapNeeded) {
                    currentData.set(j, s2);
                    currentData.set(j + 1, s1);
                }
            }
        }
        
        // --- [ส่วนที่แก้ไข: เปลี่ยนจากรูปสามเหลี่ยม เป็นข้อความธรรมดา] ---
        TableColumnModel colModel = table.getColumnModel();
        
        // 1. ล้างหัวข้อเก่า
        colModel.getColumn(0).setHeaderValue("ลำดับ");
        colModel.getColumn(1).setHeaderValue("ชื่ออุปกรณ์");
        
        // 2. เติมข้อความระบุทิศทาง (ใช้ A-Z ชัวร์กว่ารูปภาพ)
        if (ascending) {
            // ใช้เครื่องหมาย ^ หรือข้อความ (A-Z)
            colModel.getColumn(1).setHeaderValue("ชื่ออุปกรณ์ (A-Z) ^"); 
        } else {
            // ใช้เครื่องหมาย v หรือข้อความ (Z-A)
            colModel.getColumn(1).setHeaderValue("ชื่ออุปกรณ์ (Z-A) v"); 
        }
        
        // 3. บังคับวาดหัวตารางใหม่ (ใช้ resizeAndRepaint ชัวร์กว่า repaint เฉยๆ)
        table.getTableHeader().resizeAndRepaint(); 
        // ---------------------------------------------
        
        updateTable();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EquipmentListUI().setVisible(true));
    }
}