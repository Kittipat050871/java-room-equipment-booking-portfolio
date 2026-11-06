package main.ui.user;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import main.MainMenu;
import main.MainUI;
import main.MainMenu.BackgroundPanel;
import main.model.User;
import main.service.AuthService;
import main.ui.user.RoomPlan5.CircleButton;

public class AccountUI extends JFrame {
    
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JButton btnEdit;
    
    private AuthService authService; 

    public AccountUI() {
        ImageIcon windowIcon = new ImageIcon("images/logo.png");
        this.setIconImage(windowIcon.getImage());
        
        Font thaiFont = new Font("Tahoma", Font.PLAIN, 16);
        UIManager.put("Label.font", thaiFont);
        UIManager.put("Button.font", thaiFont);
        UIManager.put("TextField.font", thaiFont);
        UIManager.put("PasswordField.font", thaiFont);
        UIManager.put("OptionPane.font", thaiFont);
        UIManager.put("OptionPane.messageFont", thaiFont);
        
        this.authService = new AuthService();
        
        setTitle("บัญชีของฉัน");
        setSize(1024, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        MainMenu.BackgroundPanel bg = new MainMenu.BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        // ปุ่มย้อนกลับ
        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        // โลโก้
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

        // ชื่อระบบ (บัญชีของฉัน)
        JLabel title = new JLabel("บัญชีของฉัน");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(Color.BLACK); 
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(230, 35, 600, 40);
        bg.add(title);
        
        // ฟอร์ม
        int formWidth = 350;
        int formX = (1024 - formWidth) / 2; 
        int labelWidth = 120;
        int labelX = formX - labelWidth - 10;
        
        // 1. รหัสนักศึกษา
        JLabel lblUsername = new JLabel("รหัสนักศึกษา:");
        lblUsername.setForeground(Color.BLACK); 
        lblUsername.setHorizontalAlignment(SwingConstants.RIGHT);
        lblUsername.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblUsername.setBounds(labelX, 180, labelWidth, 30);
        bg.add(lblUsername);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Tahoma", Font.PLAIN, 18));
        txtUsername.setBounds(formX, 180, formWidth, 30);
        txtUsername.setEditable(false); 
        bg.add(txtUsername);

        // 2. อีเมล
        JLabel lblEmail = new JLabel("อีเมล:");
        lblEmail.setForeground(Color.BLACK); 
        lblEmail.setHorizontalAlignment(SwingConstants.RIGHT);
        lblEmail.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblEmail.setBounds(labelX, 230, labelWidth, 30);
        bg.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Tahoma", Font.PLAIN, 18));
        txtEmail.setBounds(formX, 230, formWidth, 30);
        txtEmail.setEditable(false); 
        bg.add(txtEmail);

        // 3. เบอร์โทรศัพท์
        JLabel lblPhone = new JLabel("เบอร์โทรศัพท์:");
        lblPhone.setForeground(Color.BLACK); 
        lblPhone.setHorizontalAlignment(SwingConstants.RIGHT);
        lblPhone.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblPhone.setBounds(labelX, 280, labelWidth, 30);
        bg.add(lblPhone);

        txtPhone = new JTextField();
        txtPhone.setFont(new Font("Tahoma", Font.PLAIN, 18));
        txtPhone.setBounds(formX, 280, formWidth, 30);
        txtPhone.setEditable(false); 
        bg.add(txtPhone);
        
        // 4. ปุ่มแก้ไข
        btnEdit = new JButton("แก้ไข");
        btnEdit.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
        btnEdit.setForeground(Color.BLACK);
        btnEdit.setBackground(new Color(126, 34, 206)); 
        btnEdit.setBounds(formX + (formWidth - 100), 340, 100, 40); 
        bg.add(btnEdit);
        
        // 5. ปุ่มลบบัญชีถาวร (เพิ่มใหม่)
        JButton btnDelete = new JButton("ลบบัญชีถาวร");
        btnDelete.setFont(new Font("Leelawadee UI", Font.BOLD, 18)); 
        btnDelete.setBackground(new Color(220, 53, 69)); // สีแดง
        btnDelete.setForeground(Color.red);
        btnDelete.setBounds(formX, 340, 160, 40); 
        bg.add(btnDelete); // [สำคัญ] ต้อง Add ลงจอ
        
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "คุณต้องการลบบัญชีนี้ถาวรใช่ไหม?\nข้อมูลประวัติการจองทั้งหมดจะหายไปและกู้คืนไม่ได้!", 
                "ยืนยันการลบ", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // เรียกใช้ฟังก์ชัน DELETE
                String currentUser = AuthService.currentLoggedInStudentId;
                boolean success = authService.deleteUserPermanently(currentUser);

                if (success) {
                    JOptionPane.showMessageDialog(this, "ลบบัญชีเรียบร้อยแล้ว\nระบบจะปิดโปรแกรม");
                    this.dispose(); 
                    new main.MainUI().setVisible(true); 
                } else {
                    JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาด ไม่สามารถลบบัญชีได้", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // ดึงข้อมูลมาโชว์
        loadUserData();
        
        btnEdit.addActionListener(e -> {
            if (btnEdit.getText().equals("แก้ไข")) {
                txtEmail.setEditable(true);
                txtPhone.setEditable(true);
                txtEmail.setBackground(Color.white); 
                txtPhone.setBackground(Color.white);
                txtEmail.requestFocus(); 
                btnEdit.setText("บันทึก"); 
                btnEdit.setBackground(new Color(22, 163, 74)); 
                btnEdit.setForeground(Color.WHITE); 
            } else {
                String studentId = AuthService.currentLoggedInStudentId;
                String newEmail = txtEmail.getText().trim();
                String newPhone = txtPhone.getText().trim();

                boolean success = authService.updateAccountDetails(studentId, newEmail, newPhone); 

                if (success) {
                    txtEmail.setEditable(false);
                    txtPhone.setEditable(false);
                    txtEmail.setBackground(new Color(240, 240, 240)); 
                    txtPhone.setBackground(new Color(240, 240, 240));
                    btnEdit.setText("แก้ไข"); 
                    btnEdit.setBackground(new Color(126, 34, 206)); 
                    btnEdit.setForeground(Color.BLACK); 
                    JOptionPane.showMessageDialog(this, "บันทึกข้อมูลเรียบร้อย!");
                } else {
                    JOptionPane.showMessageDialog(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void loadUserData() {
        String studentId = AuthService.currentLoggedInStudentId;
        if (studentId == null || studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ไม่พบข้อมูลผู้ใช้ (กรุณาล็อกอินใหม่)");
            txtUsername.setText("ERROR");
            txtEmail.setText("ERROR");
            txtPhone.setText("ERROR");
            return;
        }
        User user = authService.getUserDetails(studentId);
        if (user != null) {
            txtUsername.setText(user.getStudentId());
            txtEmail.setText(user.getEmail());
            txtPhone.setText(user.getPhone());
        } else {
            JOptionPane.showMessageDialog(this, "ไม่พบข้อมูล '" + studentId + "' ในฐานข้อมูล!");
        }
    }
}