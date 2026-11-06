package main.ui.admin;

import javax.swing.*;
import javax.swing.border.LineBorder;

import main.service.DatabaseHelper;

import java.awt.*;

public class AddEquipDialog extends JDialog {
    private JTextField txtEquipName = new JTextField(20);
    private JLabel lblCurrentAdmin = new JLabel(); 
    private JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Ready", "NotReady", "Broken"});
    private JButton btnSave = new JButton("บันทึก");
    private String adminUsername;

    public AddEquipDialog(Frame parent, DatabaseHelper db, String currentAdmin) {
        super(parent, "เพิ่มอุปกรณ์ใหม่", true);
        this.adminUsername = currentAdmin;
        
        try {
            ImageIcon sutIcon = new ImageIcon("images/logo.png");
            this.setIconImage(sutIcon.getImage());
        } catch (Exception e) {}

        setLayout(new GridLayout(4, 2, 10, 10));

        lblCurrentAdmin.setText(" " + adminUsername);
        lblCurrentAdmin.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        lblCurrentAdmin.setForeground(Color.DARK_GRAY);

        add(new JLabel("  ชื่ออุปกรณ์:")); add(txtEquipName);
        add(new JLabel("  ผู้รับผิดชอบ:")); add(lblCurrentAdmin);
        add(new JLabel("  สถานะเริ่มต้น:")); add(cbStatus);
        
        Color sutBlue = new Color(0, 122, 255); 
        btnSave.setForeground(sutBlue);          
        btnSave.setBackground(Color.WHITE);      
        btnSave.setFont(new Font("Leelawadee UI", Font.BOLD, 14));
        btnSave.setBorder(new LineBorder(sutBlue, 2)); 
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(new JLabel("")); 
        add(btnSave);

        btnSave.addActionListener(e -> {
            if (txtEquipName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณากรอกชื่ออุปกรณ์");
                return;
            }
            
            // แก้ไขจุดนี้ให้ส่ง int (จำนวน) ตาม DatabaseHelper
            db.addEquipment(txtEquipName.getText().trim(), 
                            cbStatus.getSelectedItem().toString(), 
                            1); // ส่งจำนวน 1 ชิ้น
                            
            JOptionPane.showMessageDialog(this, "เพิ่มอุปกรณ์สำเร็จ!");
            dispose();
        });

        getContentPane().setBackground(new Color(240, 240, 240));
        pack();
        setLocationRelativeTo(parent);
    }
}