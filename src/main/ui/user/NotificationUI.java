package main.ui.user;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import main.MainMenu;
import main.model.Notification;
import main.service.AuthService;
import main.ui.user.RoomPlan5.CircleButton;

public class NotificationUI extends JFrame {
    
    private AuthService authService; 

    public NotificationUI() {
        this.authService = new AuthService();
        
        ImageIcon appIcon = new ImageIcon("images/logo.png");
        this.setIconImage(appIcon.getImage());
        
        // ตั้งค่า Font
        Font thaiFont = new Font("Leelawadee UI", Font.PLAIN, 16); 
        UIManager.put("Label.font", thaiFont);
        
        setTitle("การแจ้งเตือน");
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

        JLabel title = new JLabel("การแจ้งเตือน");
        title.setFont(new Font("Leelawadee UI", Font.BOLD, 30));
        title.setBounds(230, 35, 600, 40);
        bg.add(title);
        
        // Panel สำหรับใส่รายการ (Container)
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); 
        listPanel.setBackground(Color.WHITE);
        
        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBounds(100, 120, 824, 400); 
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        bg.add(scrollPane);
        
        // --- ดึงข้อมูล Notification ---
        String currentUser = AuthService.currentLoggedInStudentId;
        List<Notification> notifications = authService.getUserNotifications(currentUser);

        if (notifications.isEmpty()) {
            JLabel lblEmpty = new JLabel("ยังไม่มีการแจ้งเตือนในขณะนี้");
            lblEmpty.setFont(new Font("Leelawadee UI", Font.ITALIC, 18));
            lblEmpty.setBorder(new EmptyBorder(20, 20, 20, 20));
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(lblEmpty);
            
        } else {
            Font titleFont = new Font("Leelawadee UI", Font.BOLD, 16);
            Font msgFont = new Font("Leelawadee UI", Font.PLAIN, 16);
            
            for (Notification noti : notifications) {
                
                JPanel rowPanel = new JPanel(new BorderLayout(15, 0)); 
                rowPanel.setBackground(Color.WHITE);
                rowPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230)),
                    new EmptyBorder(15, 20, 15, 20) 
                )); 
                rowPanel.setMaximumSize(new Dimension(800, 80));
                rowPanel.setPreferredSize(new Dimension(800, 80));
                
                // จุดแสดงสถานะ (ใช้ Polymorphism เลือกไอคอนได้ ถ้าต้องการ)
                // String iconText = noti.getIconText(); // ถ้าอยากใช้ไอคอนจาก class ลูก
                JLabel lblDot = new JLabel("•"); 
                lblDot.setFont(new Font("Tahoma", Font.BOLD, 24));
                
                // [Polymorphism] ใช้สีจาก Class ลูก (แดง/เขียว)
                try {
                    lblDot.setForeground(Color.decode(noti.getTypeColor())); 
                } catch (Exception e) {
                    lblDot.setForeground(new Color(220, 38, 38)); // Default แดง
                }
                
                rowPanel.add(lblDot, BorderLayout.WEST);

                // ส่วนข้อความ
                JPanel textPanel = new JPanel(new GridLayout(2, 1));
                textPanel.setOpaque(false);
                
                // บรรทัดวันที่
                JLabel lblDate = new JLabel(noti.getDate());
                lblDate.setFont(titleFont);
                
                // [Polymorphism] เปลี่ยนสีวันที่ตามความสำคัญก็ได้
                try {
                     lblDate.setForeground(Color.decode(noti.getTypeColor()));
                } catch (Exception e) {
                     lblDate.setForeground(new Color(150, 0, 0));
                }
                
                textPanel.add(lblDate);
                
                // บรรทัดข้อความ
                JLabel lblMsg = new JLabel(noti.getMessage());
                lblMsg.setFont(msgFont);
                lblMsg.setForeground(Color.DARK_GRAY);
                textPanel.add(lblMsg);
                
                rowPanel.add(textPanel, BorderLayout.CENTER);
                
                listPanel.add(rowPanel);
            }
        }
    }
    
    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new NotificationUI().setVisible(true));
    }
}