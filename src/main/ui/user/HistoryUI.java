package main.ui.user;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.model.Booking;
import main.service.AuthService;
import main.ui.user.RoomPlan5.CircleButton;

import java.awt.*;
import java.util.List;

public class HistoryUI extends JFrame {

    private AuthService authService;
    private JTable table;
    private DefaultTableModel model;

    public HistoryUI() {
        this.authService = new AuthService();

        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        // ฟอนต์ไทยสำหรับ dialog ต่าง ๆ
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);
        UIManager.put("Label.font", thaiFont);
        UIManager.put("Button.font", thaiFont);
        UIManager.put("TextField.font", thaiFont);
        UIManager.put("OptionPane.font", thaiFont);
        UIManager.put("OptionPane.messageFont", thaiFont);

        setTitle("ประวัติการจอง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ใช้ BackgroundPanel เดียวกับหน้าหลักเพื่อให้คุมโทน
        MainMenu.BackgroundPanel bg = new MainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== ปุ่มย้อนกลับ (CircleButton) =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        // ===== โลโก้ =====
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel logo = new JLabel(scaledIcon);
        logo.setBounds(70, 10, newWidth, newHeight);
        bg.add(logo);

        // ===== หัวข้อหลัก =====
        JLabel title = new JLabel("ประวัติการจอง");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(new Color(30, 30, 30)); // สีเข้มดูทันสมัย
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setBounds(230, 35, 600, 40);
        bg.add(title);

        // ===== หัวข้อย่อย (สถานะรวม) =====
        JLabel lblSubTitle = new JLabel("รายการจองห้องทั้งหมดของคุณ");
        lblSubTitle.setFont(new Font("Leelawadee UI", Font.BOLD, 22));
        lblSubTitle.setForeground(new Color(150, 150, 150)); // สีจางลงเพื่อไม่ให้แย่งซีนหัวข้อหลัก
        lblSubTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblSubTitle.setBounds(0, 110, 1024, 40);
        bg.add(lblSubTitle);

        // ===== ตารางประวัติการจอง (Custom UI) =====
        model = new DefaultTableModel(
                new Object[]{"รหัสการจอง", "ห้อง", "วันที่", "เวลา", "สถานะ", "อุปกรณ์"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(model);
        table.setFont(new Font("Leelawadee UI", Font.PLAIN, 16));
        table.setRowHeight(35); // เพิ่มความสูงแถวให้ดูโปร่งเหมือนฝั่ง Admin
        table.setSelectionBackground(new Color(245, 245, 245));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(false); // ปิดเส้นตารางให้ดูสะอาดตา
        table.setIntercellSpacing(new Dimension(0, 0));

        // หัวตาราง
        table.getTableHeader().setFont(new Font("Leelawadee UI", Font.BOLD, 17));
        table.getTableHeader().setBackground(new Color(250, 250, 250));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));

        // จัดขนาดคอลัมน์
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);

        // Renderer สำหรับเปลี่ยนสีสถานะ (ให้เหมือน UserDetailsUI ของ Admin)
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                JLabel c = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                c.setHorizontalAlignment(SwingConstants.CENTER);
                String status = value != null ? value.toString() : "";

                // คุมโทนสีตามสถานะ
                if ("อนุมัติ".equals(status) || "อนุมัติแล้ว".equals(status)) {
                    c.setForeground(new Color(34, 197, 94)); // เขียว Success
                } else if ("ปฏิเสธ".equals(status) || "ไม่อนุมัติ".equals(status)) {
                    c.setForeground(new Color(239, 68, 68)); // แดง Danger
                } else if ("รออนุมัติ".equals(status)) {
                    c.setForeground(new Color(242, 101, 34)); // สีส้ม SUT
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        };
        
        // จัดกลางทุกคอลัมน์เพื่อความเป็นระเบียบ
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<5; i++) {
            if(i == 4) table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer);
            else table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(100, 160, 824, 320); // ขยายกว้างขึ้นให้สมดุล
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        bg.add(scrollPane);

        // ===== ปุ่มยกเลิกการจอง (เสริมเพื่อให้ User ใช้งานได้จริง) =====
        JButton btnCancel = new JButton("ยกเลิกคำขอที่เลือก");
        btnCancel.setFont(new Font("Leelawadee UI", Font.BOLD, 15));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBackground(new Color(239, 68, 68));
        btnCancel.setFocusPainted(false);
        btnCancel.setBounds(774, 490, 150, 35);
        // bg.add(btnCancel); // ปลดคอมเมนต์บรรทัดนี้หากต้องการเพิ่มปุ่มยกเลิกด้วย

        // ===== โหลดข้อมูล =====
        loadHistory();
    }

    private void loadHistory() {
        model.setRowCount(0); 

        String studentId = AuthService.currentLoggedInStudentId;
        if (studentId == null || studentId.isBlank()) {
            JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลผู้ใช้ (กรุณาล็อกอินใหม่)");
            return;
        }

        java.util.List<Booking> history = authService.getBookingHistory(studentId);

        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "คุณยังไม่มีประวัติการจอง");
            return;
        }

        for (Booking b : history) {
            String equipments = (b.getEquipments() != null && !b.getEquipments().isBlank())
                    ? b.getEquipments()
                    : "ไม่มี";

            model.addRow(new Object[]{
                    b.getBookingId(),
                    b.getRoomName(),
                    b.getDate(),
                    b.getTimeRange(),
                    b.getStatus(),
                    equipments
            });
        }
    }
}