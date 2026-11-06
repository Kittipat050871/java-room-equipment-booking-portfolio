package main.ui.user;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu;
import main.MainMenu.BackgroundPanel; // ยืมพื้นหลัง
import main.ui.user.StartBooking.CircleButton;

public class RoomUsageRules extends JFrame {

    public RoomUsageRules() {
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());

        setTitle("ระเบียบการใช้ห้อง");
        setSize(1024, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. พื้นหลัง (ใช้ของ MainMenu)
        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(null);
        setContentPane(bg);

        CircleButton btnBack = new CircleButton();
        btnBack.setBounds(30, 30, 50, 50);
        bg.add(btnBack);

        btnBack.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        // 3. โลโก้
        ImageIcon logoIcon = new ImageIcon("images/logo.png");
        int newWidth = 120;
        int originalWidth = logoIcon.getIconWidth();
        int originalHeight = logoIcon.getIconHeight();
        if (originalWidth <= 0) originalWidth = 1;
        int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
        Image scaledImage = logoIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setBounds(100, 25, newWidth, newHeight);
        bg.add(logo);

        // 4. หัวข้อ
        JLabel title = new JLabel("ระเบียบการใช้ห้องประชุม");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setForeground(Color.BLACK);
        title.setBounds(240, 50, 400, 40);
        bg.add(title);


        // 5. กล่องเนื้อหา (โปร่งแสงนิดๆ)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        // สีขาวโปร่งแสง (Alpha 200)
        contentPanel.setBackground(new Color(255, 255, 255, 220)); 
        contentPanel.setBounds(200, 150, 624, 250);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Padding ภายใน
        bg.add(contentPanel);

        // ข้อความ 1
        JLabel txt1 = new JLabel("โปรดทราบ");
        txt1.setFont(new Font("Leelawadee UI", Font.BOLD, 24));
        txt1.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(txt1);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15))); // เว้นบรรทัด

        // ข้อความ 2
        JLabel txt2 = new JLabel("กฎระเบียบและข้อควรปฏิบัติในการใช้ห้องประชุม");
        txt2.setFont(new Font("Leelawadee UI", Font.PLAIN, 20));
        txt2.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(txt2);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 30))); // เว้นบรรทัดเยอะหน่อย

        // -----------------------------------------------------------------
        // 6. ปุ่ม PDF (Transparent Button)
        // -----------------------------------------------------------------
        JButton pdfButton = new JButton("เปิดอ่านระเบียบฉบับเต็ม (PDF)");
        pdfButton.setFont(new Font("Leelawadee UI", Font.BOLD, 18));
        pdfButton.setForeground(new Color(37, 99, 235)); // สีน้ำเงินสวยๆ
        pdfButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // ทำให้ปุ่มใส
        pdfButton.setContentAreaFilled(false); 
        pdfButton.setFocusPainted(false);
        pdfButton.setOpaque(false);
        
        // ใส่ขอบบางๆ ให้รู้ว่าเป็นปุ่ม
        pdfButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(37, 99, 235), 2, true), // ขอบสีน้ำเงิน มนๆ
                new EmptyBorder(10, 20, 10, 20) // Padding ในปุ่ม
        ));
        
        pdfButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Effect: เอาเมาส์ชี้แล้วเปลี่ยนสีพื้นหลังนิดหน่อย
        pdfButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pdfButton.setOpaque(true);
                pdfButton.setBackground(new Color(219, 234, 254)); // ฟ้าอ่อนมาก
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pdfButton.setOpaque(false);
                pdfButton.setBackground(null);
            }
        });

        // Action: เปิดไฟล์ PDF
        pdfButton.addActionListener(e -> openPdfFile());

        contentPanel.add(pdfButton);
    }

    // เมธอดเปิดไฟล์ PDF แยกออกมาให้ดูง่าย
    private void openPdfFile() {
        try {
            // ชื่อไฟล์ต้องตรงเป๊ะๆ และวางไว้หน้าเดียวกับ folder src (ไม่ใช่ข้างใน src)
            File pdfFile = new File("ระเบียบการใช้ห้องประชุมและอุปกรณ์.pdf");

            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(this, "คอมพิวเตอร์นี้ไม่รองรับการเปิดไฟล์อัตโนมัติ", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "ไม่พบไฟล์ PDF!\nกรุณาตรวจสอบว่าไฟล์ 'ระเบียบการใช้ห้องประชุมและอุปกรณ์.pdf'\nอยู่ในโฟลเดอร์โปรเจกต์แล้ว", 
                    "File Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "เปิดไฟล์ไม่ได้: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}