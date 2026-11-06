package main.ui.user;

import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;

import main.MainMenu;
import main.MainMenu.BackgroundPanel;
import main.service.AuthService;
import main.service.DatabaseConnector;
import main.ui.user.RoomPlan5.CircleButton;

public class BookingForm extends JFrame {

    private JTextField txtName;
    private JComboBox<String> cbRoom;
    private JComboBox<String> cbEquip;
    private JTextField txtPurpose;
    private JSpinner spinDate;
    private JComboBox<String> cbStart;
    private JComboBox<String> cbEnd;

    private AuthService authService;

    public BookingForm() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Font thaiFont = new Font("Leelawadee UI", Font.PLAIN, 14);
            UIManager.put("OptionPane.messageFont", thaiFont);
            UIManager.put("OptionPane.buttonFont", thaiFont);
        } catch (Exception e) { e.printStackTrace(); }
        
        this.authService = new AuthService();

        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("จองห้องและอุปกรณ์");
        setSize(1024, 600);
        setResizable(false); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        MainMenu.BackgroundPanel bg = new MainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ===== ปุ่มย้อนกลับ =====
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new StartBooking().setVisible(true);
            dispose();
        });

        // ===== โลโก้ =====
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 150;
        int originalWidth  = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(70, 10, newWidth, newHeight);
        bg.add(logo);

        // ===== หัวข้อ =====
        JLabel title = new JLabel("จองห้องและอุปกรณ์");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 32));
        title.setForeground(Color.BLACK);
        title.setBounds(230, 35, 600, 40);
        bg.add(title);

        Font labelFont = new Font("Leelawadee UI", Font.BOLD, 20);
        Font inputFont = new Font("Leelawadee UI", Font.PLAIN, 18);

        // ===== ชื่อผู้จอง =====
        JLabel lbName = new JLabel("ชื่อผู้จอง");
        lbName.setFont(labelFont);
        lbName.setBounds(120, 120, 200, 30);
        bg.add(lbName);

        txtName = new JTextField();
        txtName.setFont(inputFont);
        txtName.setBounds(250, 120, 600, 32);

        if (AuthService.currentLoggedInStudentId != null && !AuthService.currentLoggedInStudentId.isEmpty()) {
            txtName.setText(AuthService.currentLoggedInStudentId);
            txtName.setEditable(false);
        }
        bg.add(txtName);

        // ===== ห้องประชุม =====
        JLabel lbRoom = new JLabel("ห้องประชุม");
        lbRoom.setFont(labelFont);
        lbRoom.setBounds(120, 170, 200, 30);
        bg.add(lbRoom);

        cbRoom = new JComboBox<>(new String[]{
                "--- เลือกห้อง ---",
                "ห้องเล็ก01", "ห้องเล็ก02", "ห้องเล็ก03", "ห้องเล็ก04", "ห้องเล็ก05",
                "ห้องใหญ่01", "ห้องใหญ่02"
        });
        cbRoom.setFont(inputFont);
        cbRoom.setBounds(250, 170, 200, 32);
        cbRoom.setFocusable(false); 
        bg.add(cbRoom);

        // ===== อุปกรณ์ =====
        JLabel lbEquip = new JLabel("อุปกรณ์");
        lbEquip.setFont(labelFont);
        lbEquip.setBounds(480, 170, 200, 30);
        bg.add(lbEquip);

        cbEquip = new JComboBox<>();
        cbEquip.setFont(inputFont);
        cbEquip.setBounds(590, 170, 260, 32);
        cbEquip.setFocusable(false); 
        
        cbEquip.setRenderer(new EquipmentStatusRenderer());
        
        cbEquip.addActionListener(e -> {
            String selectedItem = (String) cbEquip.getSelectedItem();
            if (selectedItem != null) {
                if (selectedItem.contains("(ชำรุด)") || selectedItem.contains("(ไม่พร้อมใช้งาน)")) {
                    JOptionPane.showMessageDialog(this, 
                        "อุปกรณ์นี้ไม่พร้อมใช้งาน กรุณาเลือกรายการอื่น", 
                        "แจ้งเตือน", JOptionPane.WARNING_MESSAGE);
                    cbEquip.setSelectedIndex(0);
                }
            }
        });
        
        bg.add(cbEquip);
        loadEquipment();

        // ===== วันที่ =====
        JLabel lbDate = new JLabel("วันที่");
        lbDate.setFont(labelFont);
        lbDate.setBounds(120, 220, 200, 30);
        bg.add(lbDate);

        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        spinDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinDate, "dd/MM/yyyy");
        spinDate.setEditor(dateEditor);
        spinDate.setFont(inputFont);
        spinDate.setBounds(250, 220, 200, 32);
        bg.add(spinDate);

        // ===== วัตถุประสงค์ =====
        JLabel lbPurpose = new JLabel("วัตถุประสงค์");
        lbPurpose.setFont(labelFont);
        lbPurpose.setBounds(480, 220, 200, 30);
        bg.add(lbPurpose);

        txtPurpose = new JTextField();
        txtPurpose.setFont(inputFont);
        txtPurpose.setBounds(590, 220, 260, 32);
        bg.add(txtPurpose);

        // ===== เวลา =====
        JLabel lbStart = new JLabel("เวลาเริ่ม");
        lbStart.setFont(labelFont);
        lbStart.setBounds(120, 270, 200, 30);
        bg.add(lbStart);

        String[] times = {
                "08:00","09:00","10:00","11:00","12:00",
                "13:00","14:00","15:00","16:00","17:00",
                "18:00","19:00"
        };

        cbStart = new JComboBox<>(times);
        cbStart.setFont(inputFont);
        cbStart.setBounds(250, 270, 200, 32);
        cbStart.setFocusable(false); 
        bg.add(cbStart);

        JLabel lbEnd = new JLabel("สิ้นสุด");
        lbEnd.setFont(labelFont);
        lbEnd.setBounds(480, 270, 200, 30);
        bg.add(lbEnd);

        cbEnd = new JComboBox<>(times);
        cbEnd.setFont(inputFont);
        cbEnd.setBounds(590, 270, 260, 32);
        cbEnd.setFocusable(false); 
        bg.add(cbEnd);

        // =================================================================
        // [แก้ไข] ใช้ปุ่มแบบมีเงา (ColorShadowButton)
        // =================================================================
        
        // ===== ปุ่มบันทึก (สีน้ำเงินเงา) =====
        JButton btnSave = new ColorShadowButton("บันทึก", new Color(37, 99, 235));
        btnSave.setBounds(430, 350, 120, 40);
        bg.add(btnSave);

        // ===== ปุ่มยกเลิก (สีแดงเงา) =====
        JButton btnCancel = new ColorShadowButton("ยกเลิก", new Color(220, 38, 38));
        btnCancel.setBounds(570, 350, 120, 40);
        bg.add(btnCancel);
        // =================================================================

        btnCancel.addActionListener(e -> {
            new StartBooking().setVisible(true);
            dispose();
        });

        // ===== Action: บันทึก =====
        btnSave.addActionListener(e -> {
            String user = AuthService.currentLoggedInStudentId;
            if (user == null || user.isBlank()) {
                JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลผู้ใช้ (กรุณาล็อกอินใหม่)");
                return;
            }

            String room = (String) cbRoom.getSelectedItem();
            if (room == null || room.equals("--- เลือกห้อง ---")) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกห้องประชุม");
                return;
            }

            String equipItem = (String) cbEquip.getSelectedItem();
            if (equipItem == null || equipItem.equals("--- เลือกอุปกรณ์ ---")) {
                JOptionPane.showMessageDialog(this, "กรุณาเลือกอุปกรณ์");
                return;
            }
            if (equipItem.contains("(ชำรุด)") || equipItem.contains("(ไม่พร้อมใช้งาน)")) {
                JOptionPane.showMessageDialog(this, "อุปกรณ์นี้ไม่พร้อมใช้งาน");
                return;
            }

            String purpose = txtPurpose.getText().trim();
            if (purpose.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกวัตถุประสงค์");
                return;
            }

            Date dateVal = (Date) spinDate.getValue();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String dateStr = dateFormat.format(dateVal);

            String startStr = (String) cbStart.getSelectedItem();
            String endStr   = (String) cbEnd.getSelectedItem();

            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date startTime = timeFormat.parse(startStr);
                Date endTime   = timeFormat.parse(endStr);

                if (!endTime.after(startTime)) {
                    JOptionPane.showMessageDialog(this, "เวลา 'สิ้นสุด' ต้องมากกว่า 'เวลาเริ่ม'");
                    return;
                }
                
                // Double Booking Check
                int roomId = authService.getRoomIdByName(room);
                String checkStartDateTime = dateStr + "T" + startStr;
                String checkEndDateTime   = dateStr + "T" + endStr;
                
                if (!authService.isRoomAvailable(roomId, checkStartDateTime, checkEndDateTime)) {
                    JOptionPane.showMessageDialog(this, 
                        "❌ ไม่สามารถจองได้!\nห้อง " + room + " ถูกจองในช่วงเวลานี้ไปแล้ว", 
                        "เวลาชนกัน", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            int equipmentId = 0;
            int quantity = 1;
            
            if (!equipItem.equals("ไม่ประสงค์ยืม") && equipItem.contains(":")) {
                try {
                    equipmentId = Integer.parseInt(equipItem.split(":")[0]);
                } catch (NumberFormatException ex) {
                    equipmentId = 0;
                }
            }

            boolean success = authService.createBooking(
                    user, room, dateStr, startStr, endStr,
                    purpose, equipmentId, quantity);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ จองสำเร็จ! ระบบส่งข้อมูลไปยังหน้าสถานะของคุณแล้ว");
                new BookingStatusUI().setVisible(true);
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "บันทึกไม่สำเร็จ! (Database Error)");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookingForm().setVisible(true));
    }

    private void loadEquipment() {
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT equipment_id, equipment_name, status FROM equipment")) {

            cbEquip.removeAllItems();
            cbEquip.addItem("--- เลือกอุปกรณ์ ---");
            cbEquip.addItem("ไม่ประสงค์ยืม");

            while (rs.next()) {
                int id = rs.getInt("equipment_id");
                String rawName = rs.getString("equipment_name"); 
                String status = rs.getString("status");
                
                String fullName = convertToFullName(rawName);
                String displayText = id + ":" + fullName;
                
                if (status != null) {
                    if (status.equals("Broken")) {
                        displayText += " (ชำรุด)";
                    } else if (status.equals("NotReady")) {
                        displayText += " (ไม่พร้อมใช้งาน)";
                    }
                }
                cbEquip.addItem(displayText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String convertToFullName(String rawName) {
        if (rawName == null) return "";
        if (rawName.startsWith("PP")) return rawName.replace("PP", "iPad Pro ");
        if (rawName.startsWith("PG")) return rawName.replace("PG", "iPad Gen 11 (") + ")"; 
        if (rawName.startsWith("C")) return rawName.replace("C", "Camera + Tripod ");
        return rawName; 
    }
    
    class EquipmentStatusRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                String text = value.toString();
                
                // แก้ตรงนี้ครับ: แยกเงื่อนไขเพื่อใส่คนละสี
                if (text.contains("(ชำรุด)")) {
                    setForeground(Color.ORANGE); // <--- เปลี่ยนชำรุดเป็นสีส้ม
                } else if (text.contains("(ไม่พร้อมใช้งาน)")) {
                    setForeground(Color.RED);    // <--- ไม่พร้อมใช้งานเป็นสีแดง
                } else {
                    setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
    // =========================================================================
    // [เพิ่มใหม่] คลาสปุ่มพิเศษ (บังคับสีพื้นหลัง + มีเงา)
    // =========================================================================
    class ColorShadowButton extends JButton {
        private Color baseColor;
        private Color shadowColor = new Color(0, 0, 0, 50); 

        public ColorShadowButton(String text, Color bg) {
            super(text);
            this.baseColor = bg;
            
            setFont(new Font("Leelawadee UI", Font.BOLD, 18)); // ปรับฟอนต์ให้ใหญ่ขึ้นนิดนึงให้สมดุล
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = 4;
            g2.setColor(shadowColor);
            g2.fillRoundRect(shadowOffset, shadowOffset, getWidth()-shadowOffset, getHeight()-shadowOffset, 20, 20);

            if (getModel().isPressed()) {
                g2.setColor(baseColor.darker());
                g2.translate(2, 2); 
            } else if (getModel().isRollover()) {
                g2.setColor(baseColor.brighter());
            } else {
                g2.setColor(baseColor);
            }
            
            g2.fillRoundRect(0, 0, getWidth()-shadowOffset, getHeight()-shadowOffset, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}