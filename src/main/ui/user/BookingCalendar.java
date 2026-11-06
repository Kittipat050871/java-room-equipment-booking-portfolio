package main.ui.user;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.service.DatabaseConnector;
import main.ui.user.StartBooking.CircleButton; 

public class BookingCalendar extends JFrame {
    
    private DefaultTableModel model; 
    private JLabel lblDateTitle; 
    private LocalDate currentDate; 
    private JTable table;
    
    private DateTimeFormatter titleFormatter;
    
    // Theme Colors
    private final Color THEME_RED = new Color(220, 38, 38);   
    private final Color THEME_DARK = new Color(50, 50, 50);   
    private final Color THEME_TEXT = new Color(30, 30, 30);   

    private final String[] ROOM_NAMES = {
        "ห้องเล็ก01", "ห้องเล็ก02", "ห้องเล็ก03", "ห้องเล็ก04", "ห้องเล็ก05",
        "ห้องใหญ่01", "ห้องใหญ่02"
    };

    public BookingCalendar() {
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("ตารางการใช้ห้องรายวัน");
        setSize(1024, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Locale thaiLocale = new Locale("th", "TH");
        titleFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", thaiLocale); 
        
        this.currentDate = LocalDate.now();

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new StartBooking().setVisible(true);
            dispose();
        });
        
        // Logo
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 120; 
        int newHeight = (int) ((double) logoIcon.getIconHeight() / logoIcon.getIconWidth() * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(100, 25, newWidth, newHeight); 
        bg.add(logo);

        JLabel title = new JLabel("ตารางการใช้ห้องรายวัน");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(Color.BLACK); 
        title.setBounds(240, 45, 400, 40);
        bg.add(title);

        // Legend
        int legendY = 110; 
        int legendX = 750; 
        bg.add(createLegendBox(legendX, legendY, THEME_RED, "ไม่ว่าง (จองแล้ว)")); 
        bg.add(createLegendBox(legendX + 130, legendY, Color.WHITE, "ว่าง"));           

        // Nav Buttons
        JButton btnPrev = new JButton("<");
        styleNavButton(btnPrev);
        btnPrev.setBounds(80, 130, 50, 30); 
        bg.add(btnPrev);

        lblDateTitle = new JLabel("...");
        lblDateTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
        lblDateTitle.setForeground(THEME_TEXT);
        lblDateTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblDateTitle.setBounds(140, 130, 250, 30); 
        bg.add(lblDateTitle);
        
        JButton btnNext = new JButton(">");
        styleNavButton(btnNext);
        btnNext.setBounds(400, 130, 50, 30); 
        bg.add(btnNext);

        // Table
        String[] columns = {
            "รายชื่อห้อง",
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00", 
            "16:00", "17:00", "18:00"
        };

        this.model = new DefaultTableModel(null, columns) { 
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; 
            }
        };

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        table.setFocusable(false);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);

        table.setFont(new Font("Leelawadee UI", Font.PLAIN, 13));
        table.setRowHeight(50);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setReorderingAllowed(false); 
        header.setResizingAllowed(false);   
        
        TableColumnModel colModel = table.getColumnModel();
        colModel.getColumn(0).setPreferredWidth(150); 
        for (int i = 1; i < columns.length; i++) {
            colModel.getColumn(i).setPreferredWidth(90); 
        }

        // Custom Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(
                            tbl, value, false, false, row, column);
                
                if (column == 0) { 
                    c.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
                    c.setBackground(THEME_DARK); 
                    setForeground(Color.WHITE);
                    setHorizontalAlignment(CENTER);
                } else { 
                    c.setFont(new Font("Leelawadee UI", Font.PLAIN, 12)); 
                    setHorizontalAlignment(CENTER);
                    
                    String text = (value != null) ? value.toString() : "";
                    
                    if (!text.isEmpty()) {
                        c.setBackground(THEME_RED); 
                        c.setForeground(Color.WHITE);
                        ((JComponent) c).setToolTipText("ผู้จอง: " + text);
                    } else {
                        c.setBackground(Color.WHITE); 
                        c.setForeground(Color.GRAY);
                        ((JComponent) c).setToolTipText(null);
                    }
                }
                
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(0, 5, 0, 5));
                }
                return c;
            }
        });
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(80, 170, 880, 375);
        scroll.getViewport().setBackground(Color.WHITE);
        bg.add(scroll);
        
        btnPrev.addActionListener(e -> changeDate(-1));
        btnNext.addActionListener(e -> changeDate(1));

        updateCalendar();
    }
    
    // ================= Logic =================
    
    private void updateCalendar() {
        model.setRowCount(0);
        lblDateTitle.setText(currentDate.format(titleFormatter));

        List<String>[][] slotNames = new ArrayList[ROOM_NAMES.length][12];
        for (int i = 0; i < ROOM_NAMES.length; i++) {
            for (int j = 0; j < 12; j++) {
                slotNames[i][j] = new ArrayList<>();
            }
        }

        // ====================================================================
        // [แก้ไข] เลือกเฉพาะ 'อนุมัติ' และ 'อนุมัติแล้ว' (ตัด 'รออนุมัติ' ทิ้ง)
        // ====================================================================
        String sql = "SELECT room_id, start_time, end_time, status, username FROM bookings " +
                     "WHERE status IN ('อนุมัติ', 'อนุมัติแล้ว') " + 
                     "AND start_time >= ? AND start_time <= ?";
        
        String queryStart = currentDate.toString() + "T00:00";
        String queryEnd   = currentDate.toString() + "T23:59";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, queryStart);
            pstmt.setString(2, queryEnd);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int roomId      = rs.getInt("room_id");
                    String startStr = rs.getString("start_time");
                    String endStr   = rs.getString("end_time");
                    String user     = rs.getString("username");
                    
                    if (startStr == null || endStr == null) continue;
                    
                    LocalDateTime startDT = LocalDateTime.parse(startStr);
                    LocalDateTime endDT   = LocalDateTime.parse(endStr);
                    
                    int rowIndex = roomId - 1; 
                    if (rowIndex < 0 || rowIndex >= ROOM_NAMES.length) continue;

                    int startHour = startDT.getHour();
                    int endHour   = endDT.getHour();
                    
                    String displayText = user;
                    
                    // รวมเวลาจบด้วย (ตามที่ขอไว้ก่อนหน้า)
                    for (int h = startHour; h <= endHour; h++) {
                        if (h == endHour && startHour != endHour) { } 

                        int colIndex = h - 7; 
                        if (colIndex >= 1 && colIndex <= 11) {
                            if (user != null) {
                                slotNames[rowIndex][colIndex].add(displayText);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < ROOM_NAMES.length; i++) {
            Object[] rowData = new Object[12];
            rowData[0] = ROOM_NAMES[i]; 
            for (int col = 1; col < 12; col++) {
                List<String> names = slotNames[i][col];
                if (names.isEmpty()) {
                    rowData[col] = ""; 
                } else {
                    rowData[col] = String.join(", ", names);
                }
            }
            model.addRow(rowData);
        }
    }
    
    private void changeDate(int daysToAdd) {
        currentDate = currentDate.plusDays(daysToAdd);
        updateCalendar();
    }

    private JPanel createLegendBox(int x, int y, Color color, String text) {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        p.setBounds(x, y, 140, 30);
        
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        colorBox.setBounds(0, 5, 20, 20);
        
        JLabel lb = new JLabel(text);
        lb.setFont(new Font("Leelawadee UI", Font.PLAIN, 14));
        lb.setForeground(Color.BLACK); 
        lb.setBounds(30, 5, 110, 20);
        
        p.add(colorBox);
        p.add(lb);
        return p;
    }
    
    private void styleNavButton(JButton btn) {
        btn.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
    }
}